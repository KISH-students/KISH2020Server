package org.kish2020.utils;

import io.github.bangjunyoung.KoreanChar;
import org.apache.commons.lang3.StringUtils;
import org.kish2020.MainLogger;
import org.kish2020.entity.Post;
import org.kish2020.entity.PostInfo;

import java.io.File;
import java.util.*;

public class Utils {
    public static String makeChoseongSentence(String sentence){
        StringBuilder choseongSb = new StringBuilder();
        for(char c : sentence.toCharArray()){
            if(KoreanChar.isSyllable(c)) c = KoreanChar.getCompatChoseong(c);
            choseongSb.append(c);
        }
        return choseongSb.toString();
    }

    public static ArrayList<String> getContentTokens(String postContent){
        if(postContent == null || postContent.isEmpty()) return null;
        ArrayList<String> list = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(postContent);
        while (st.hasMoreTokens()){
            list.add(st.nextToken());
        }
        return list;
    }

    public static LinkedHashMap<String, Integer> getContentTokenMap(String postContent){
        if(postContent == null || postContent.isEmpty()) return null;
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        StringTokenizer st = new StringTokenizer(postContent);
        while (st.hasMoreTokens()){
            String token = st.nextToken();
            map.put(token, map.getOrDefault(token, 0) + 1);
        }
        return map;
    }

    public static void addPostToKeyword(String postKey, String title, LinkedHashMap<String, String> attachmentUrlMap, LinkedHashMap<String, HashMap<String, Long>> targetKeywordMap, LinkedHashMap<String, Integer> tokenMap){
        if(tokenMap == null){
            MainLogger.error(postKey + "의 tokenMap이 null입니다.");
            MainLogger.error("해당 게시물의 본문이 비어있음을 뜻할 수 있습니다.");
            return;
        }
        tokenMap = (LinkedHashMap<String, Integer>) tokenMap.clone();
        tokenMap.putAll(Utils.getContentTokenMap(title));
        for(String attachmentName : attachmentUrlMap.keySet()) {
            tokenMap.putAll(Utils.getContentTokenMap(attachmentName));
        }
        for(String key : tokenMap.keySet()){
            long count = tokenMap.get(key);
            HashMap<String, Long> keywordData = targetKeywordMap.getOrDefault(key, new HashMap<>());
            keywordData.put(postKey, count);
            String choseongKeywordData = makeChoseongSentence(key);
            HashMap<String, Long> choseongMap = targetKeywordMap.getOrDefault(choseongKeywordData, new HashMap<>());
            choseongMap.put(postKey, count);

            targetKeywordMap.put(key, keywordData);
            targetKeywordMap.put(choseongKeywordData, choseongMap);
        }
        MainLogger.info(postKey + "의 " + tokenMap.size() + "개의 토큰이 저장됨");
    }

    /* 리팩토링 필요 */
    public static void removePostFromKeyword(String postKey, String title, LinkedHashMap<String, String> attachmentUrlMap, LinkedHashMap<String, HashMap<String, Long>> targetKeywordMap, LinkedHashMap<String, Integer> tokenMap){
        tokenMap = (LinkedHashMap<String, Integer>) tokenMap.clone();
        tokenMap.putAll(Utils.getContentTokenMap(title));
        for(String attachmentName : attachmentUrlMap.keySet()) {
            tokenMap.putAll(Utils.getContentTokenMap(attachmentName));
        }
        for(String key : tokenMap.keySet()){
            HashMap<String, Long> keywordData = targetKeywordMap.getOrDefault(key, new HashMap<>());
            keywordData.remove(postKey);
            String choseongKeywordData = makeChoseongSentence(key);
            HashMap<String, Long> choseongMap = targetKeywordMap.getOrDefault(choseongKeywordData, new HashMap<>());
            choseongMap.remove(postKey);

            targetKeywordMap.put(key, keywordData);
            targetKeywordMap.put(choseongKeywordData, choseongMap);
        }
        MainLogger.info(postKey + "의 " + tokenMap.size() + "개의 토큰이 제거됨");
    }

    // TODO : 검색결과 fix
    //TreeMap<Long, HashSet<String>>
    public static ArrayList<String>  search(LinkedHashMap<String, HashMap<String, Long>> sourceMap, String keyword){
        String choseongKeyword = Utils.makeChoseongSentence(keyword);
        String noSpaceKeyword = StringUtils.replace(keyword, " ", "");
        //TreeMap<Long, HashSet<String>> treeMap = new TreeMap<>();
        HashSet<String> addedPost = new HashSet<>();
        /*for(String key : sourceMap.keySet()){
            if(keyword.contains(key) || choseongKeyword.equals(key) || noSpaceKeyword.contains(key) || key.contains(noSpaceKeyword)){
            if(noSpaceKeyword.contains(key)){
                for(String postKey : sourceMap.get(key).keySet()){
                    if(addedPost.contains(postKey)) continue;
                    long i = sourceMap.get(key).get(postKey);    // 횟수
                    HashSet<String> set = treeMap.getOrDefault(i, new HashSet<>()); //treemap의 i에 있는 postkey set
                    set.add(postKey);
                    treeMap.put(i, set);
                    addedPost.add(postKey);
                }
            }
        }*/

        //임시
        HashMap<String, Integer> srcMap = new HashMap<>();
        String[] keywords = keyword.split(" ");
        for(String key : sourceMap.keySet()){
            for(String token : keywords){
                token = token.trim();
                if(key.contains(token)) {
                    for (String postKey : sourceMap.get(key).keySet()) {
                        srcMap.put(postKey, srcMap.getOrDefault(postKey, 0) + 1);
                        /*if (addedPost.contains(postKey)) continue;
                        long i = sourceMap.get(key).get(postKey);    // 횟수
                        HashSet<String> set = treeMap.getOrDefault(i, new HashSet<>()); //treemap의 i에 있는 postkey set
                        set.add(postKey);
                        treeMap.put(i, set);
                        addedPost.add(postKey);*/
                    }
                }
            }
        }

        ArrayList<String> keyList = new ArrayList<>(srcMap.keySet());
        Collections.sort(keyList, (o1, o2) -> (srcMap.get(o2).compareTo(srcMap.get(o1))));
        return keyList;
    }

    /**
     * post/posts/에 저장된 게시물인지 확인합니다.
     */
    public static boolean isSavedPost(String postKey){
        String[] tokens = postKey.split(",");
        return isSavedPost(tokens[0], tokens[1]);
    }

    public static boolean isSavedPost(String menuId, String postId){
        File file = new File("post/posts/" + menuId + "/" + postId + ".json");
        return file.exists();
    }

    /**
     * ./post/posts 에 저장된 게시물들을 postInfo로 가져옵니다.
     */
    public static LinkedHashMap<String, PostInfo> getAllPostInfoFromPost(){
        LinkedHashMap<String, PostInfo> map = new LinkedHashMap<>();
        File[] indexes = (new File("post/posts")).listFiles();
        for(File indexFolder : indexes){
            if(indexFolder.isDirectory()){
                String menuID = indexFolder.getName();
                File[] files = indexFolder.listFiles();
                for(File postFile : files){
                    String[] tokens = postFile.getName().split("[.]");
                    if(tokens.length < 2){
                        MainLogger.info("post id 분석 실패 : " + postFile.getName());
                        continue;
                    }
                    String PostID = tokens[0];
                    Post post = new Post(menuID, PostID, false);
                    PostInfo postInfo = new PostInfo(post);
                    map.put(menuID + "," + PostID, postInfo);
                }
            }
        }
        return map;
    }
}