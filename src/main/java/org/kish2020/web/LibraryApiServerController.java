package org.kish2020.web;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kish2020.DataBase.DataBase;
import org.kish2020.Kish2020Server;
import org.kish2020.MainLogger;
import org.kish2020.entity.RequestResult;
import org.kish2020.utils.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

@Controller
@RequestMapping("/api/library")
public class LibraryApiServerController {
    private Kish2020Server main;
    private DataBase db;    // TODO : 책 목록 저장 및 불러오기

    public HashMap<String, String> session = new HashMap<>();

    public LibraryApiServerController(Kish2020Server main){
        this.main = main;
        this.db = main.getMainDataBase();
    }

    /**
     * 해당 ID가 이미 존재하는 ID인지 확인합니다
     * @param seq 회원 ID
     * @param id 입력한 ID
     * @param pwd 입력한 비밀번호
     * @param ck 재입력 비밀번호
     * */

    @RequestMapping(value = "/checkID", method = RequestMethod.POST)    //TODO : 테스트
    public @ResponseBody String checkID(@RequestParam String seq, @RequestParam String id, @RequestParam String pwd, @RequestParam(required = false, defaultValue = "") String ck){
        String parameters;
        /*ID_EXIST_CHECK 값이 무엇을 의미하는지는 모르겠습니다만, 회원가입 중복확인시 0을 사용합니다 --> 1도 사용되네요..?*/
        parameters = "ID_EXIST_CHECK=" + 0;
        parameters += "&MEMBER_REG_SEQ=" + seq;
        parameters += "&MEMBER_REG_ID=" + id;
        parameters += "&MEMBER_REG_PWD=" + pwd;
        parameters += "&MEMBER_PWD_CK=" + ck;

        /*
        * message : 결과 메세지
        * result가 0이면 사용 가능 아이디
        */
        JSONObject response = WebUtils.postRequestWithJsonResult("http://lib.hanoischool.net:81/front/member/checkID", WebUtils.ContentType.FORM, parameters);
        return response.toJSONString();
    }

    //http://lib.hanoischool.net:81/front/member/register
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public @ResponseBody String register(@RequestParam String uuid, @RequestParam String seq, @RequestParam String id, @RequestParam String pwd, @RequestParam String ck){
        String parameters;
        parameters = "ID_EXIST_CHECK=" + 1;
        parameters += "&MEMBER_REG_SEQ=" + seq;
        parameters += "&MEMBER_REG_ID=" + id;
        parameters += "&MEMBER_REG_PWD=" + pwd;
        parameters += "&MEMBER_PWD_CK=" + ck;

        JSONObject response = WebUtils.postRequestWithJsonResult("http://lib.hanoischool.net:81/front/member/register", WebUtils.ContentType.FORM, parameters, this.getCookie(uuid));
        return response.toJSONString();
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public @ResponseBody String login(@RequestParam String uuid, @RequestParam String id, @RequestParam String pwd){
        String parameters;
        parameters = "REQ_URL=";
        parameters += "&MEMBER_ID=" + id;
        parameters += "&MEMBER_PWD=" + pwd;

        RequestResult response = WebUtils.postRequest("http://lib.hanoischool.net:81/front/login", WebUtils.ContentType.FORM, parameters, this.getCookie(uuid));
        String json = response.getResponse();
        return json;
    }

    @RequestMapping(value = "/getInfo", method = RequestMethod.GET)
    public @ResponseBody String getInfo(@RequestParam String uuid){
        JSONObject json = new JSONObject();
        if(!this.session.containsKey(uuid)){
            json.put("result", "1");
            return json.toJSONString();
        }
        String response = WebUtils.getRequest("http://lib.hanoischool.net:81/front/mypage/mypage", this.getCookie(uuid)).getResponse();
        Document doc = Jsoup.parse(response);
        Elements myPageBox = doc.select(".list-style01");
        if(myPageBox.size() < 2){
            json.put("result", "1");
            return json.toJSONString();
        }
        Elements myInfoElements = myPageBox.get(0).select("span");
        Elements loanInfoElements = myPageBox.get(1).select("span");
        json.put("result", "0");
        json.put("id", myInfoElements.get(0).text());
        json.put("name", myInfoElements.get(1).text());
        json.put("grade", myInfoElements.get(2).text());
        json.put("numberLoanableBooks", myInfoElements.get(3).text());
        json.put("loanRestrictionDate", myInfoElements.get(4).text());

        json.put("numberLoanBooks", loanInfoElements.get(0).text());
        json.put("numberOverdueBooks", loanInfoElements.get(1).text());
        json.put("numberReservedBooks", loanInfoElements.get(2).text());
        return json.toJSONString();
    }

    @RequestMapping(value = "/getLoanedBooks")
    public @ResponseBody String getLoanedBooks(@RequestParam String uuid){
        if(!this.session.containsKey(uuid)){
            return "[-1]";  // 비로그인 상태일경우 index 0에 -1 반환
        }
        JSONArray jsonArray = new JSONArray();
        String response = WebUtils.getRequest("http://lib.hanoischool.net:81/front/mypage/bookLend?SC_BOOKSTATUS_CK=on&SC_BOOKSTATUS_FLAG=0", this.getCookie(uuid)).getResponse();
        Document doc = Jsoup.parse(response);
        Elements books = doc.select("tbody").get(1).select("tr");
        for(Element book : books){
            Elements infoElements = book.select("td");
            MainLogger.error(infoElements.toString());
            HashMap<String, String> map = new HashMap<>();
            map.put("id", infoElements.get(0).text());
            map.put("registrationNumber", infoElements.get(1).text());
            map.put("billingSymbol", infoElements.get(2).text());
            map.put("bookName", infoElements.get(3).text());
            map.put("loanDate", infoElements.get(4).text());
            map.put("returnScheduledDate", infoElements.get(5).text());
            map.put("returnDate", infoElements.get(6).text());
            jsonArray.add(map);
        }
        return jsonArray.toJSONString();
    }

    /**
     * KISH 학생 여부를 확인합니다
     * @param name 본명
     * @param seq 도서관 회원 ID (대출증 id)*/

    @RequestMapping(value = "/isMember", method = RequestMethod.POST)   // TODO : 테스트
    public @ResponseBody String isMember(@RequestParam String name, @RequestParam String seq){
        try {
            name = URLEncoder.encode(name,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            MainLogger.error("name 인코딩중 오류가 발생하였습니다.", e);
            return "{\"message\":\"요청을 처리하는도중 오류가 발생하였습니다.\",\"result\":500}";
        }
        String parameters = "MEMBER_NM=" + name + "&MEMBER_SEQ=" + seq;
        /* result 2 : 이미 가입된 (웹)회원
           result 1 : 존재하지 않는 (도서관)회원
         */
        JSONObject result = WebUtils.postRequestWithJsonResult("http://lib.hanoischool.net:81/front/member/search", WebUtils.ContentType.FORM, parameters);
        return result.toJSONString();
    }

    @RequestMapping(value = "/findID", method = RequestMethod.POST)
    public @ResponseBody String findID(@RequestParam String name, @RequestParam String seq){
        try {
            name = URLEncoder.encode(name,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            MainLogger.error("name 인코딩중 오류가 발생하였습니다.", e);
            return "{\"message\":\"요청을 처리하는도중 오류가 발생하였습니다.\",\"result\":1}";
        }
        String parameters = "FORM_MODE=ID&MEMBER_REG_ID=&MEMBER_NM=" + name + "&MEMBER_SEQ=" + seq;
        // result 0 : 성공, 1 : 아이디 존재 X
        JSONObject result = WebUtils.postRequestWithJsonResult("http://lib.hanoischool.net:81/front/member/searchID", WebUtils.ContentType.FORM, parameters, "");
        return result.toJSONString();
    }

    @RequestMapping(value = "/findPWD", method = RequestMethod.POST)
    public @ResponseBody String findPWD(@RequestParam String id, @RequestParam String name, @RequestParam String seq){
        try {
            name = URLEncoder.encode(name,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            MainLogger.error("name 인코딩중 오류가 발생하였습니다.", e);
            return "{\"message\":\"요청을 처리하는도중 오류가 발생하였습니다.\",\"result\":1}";
        }
        String parameters = "FORM_MODE=PWD&MEMBER_REG_ID=" + id + "&MEMBER_NM=" + name + "&MEMBER_SEQ=" + seq;
        // result 0 : 성공, 1 : 정보 일치 회원 X
        // RESULT_VALUE에 설정된 임시 pwd 전달됨
        JSONObject result = WebUtils.postRequestWithJsonResult("http://lib.hanoischool.net:81/front/member/searchPWD", WebUtils.ContentType.FORM, parameters, "");
        return result.toJSONString();
    }

    public String getCookie(String uuid){
        String cookie;
        if(!this.session.containsKey(uuid)) {
            this.session.put(uuid, WebUtils.getNewCookie("http://lib.hanoischool.net:81/"));
        }
        cookie = this.session.get(uuid);
        return cookie;
    }

}
