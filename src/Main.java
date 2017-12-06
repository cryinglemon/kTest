import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {


    public static void main(String[] args) {

        List<String> resultList = fileRead("temp/comments.csv", "UTF-8");

        List<String> school = schoolList(resultList);

        groupby(school);



    }

    /**
     * 그룹 별로 카운팅
     * @param school
     */
    public static void groupby(List<String> school) {
        Map<String, Long> counted = school.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        System.out.println(counted);
    }




    /**
     * 학교 정보 얻기
     * @param lineList
     * @return
     */
    public static List<String> schoolList(List<String> lineList) {
        String notMatch =  "초등학교|중학교|고등학교|대학교";
        List<String> resultList = new ArrayList<String>();
        lineList.forEach(str -> {
            Pattern p = Pattern.compile("[ㄱ-ㅎ가-힣]{0,10}중학교|[ㄱ-ㅎ가-힣]{0,10}등학교|[ㄱ-ㅎ가-힣]{0,10}대학교");
            Matcher m = p.matcher(str);
            while (m.find()) {
                if(!Pattern.matches(notMatch, m.group()) && veri(m.group())) {
                    resultList.add(m.group());
                    System.out.println(m.group());
                }
            }
        });
        System.out.println(resultList.size());
        return resultList;
    }


    /**
     * 파일 읽기
     * @param path
     * @param encoding
     * @return
     */
    private static List<String> fileRead(String path, String encoding) {
        List<String> resultList = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null) {
                resultList.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(br != null) try {br.close(); } catch (IOException e) {}
        }
        return resultList;
    }


    /**
     * API 호출시 구분값 지정
     * @param school
     * @return
     */
    public static String gubunType(String school) {
        String type  = "";
        // 초등학교 : elem_list, 중학교 : midd_list , 고등학교 : high_list , 대학교 : univ_list

        if (Pattern.matches(".*초등학교", school)  ) {
            type = "elem_list";
        } else if (Pattern.matches(".*중학교", school)  ) {
            type = "midd_list";
        } else if (Pattern.matches(".*고등학교", school)  ) {
            type = "high_list";
        } else  if (Pattern.matches(".*대학교", school)  ) {
            type = "univ_list";
        }

        return type;
    }

    /**
     * API 를 이용한 학교 검증
     * @param school
     * @return
     */
    public static Boolean veri(String school) {

        boolean returnBoolen = false;
        BufferedReader bufferedReader = null;

        try {
            String gubun = gubunType(school);

            //링크 주소 만들기
            String requestUrl = "http://www.career.go.kr/cnet/openapi/getOpenApi";
            requestUrl += "?apiKey=cf94c76cf3ef492a7f68df458e646ef5&svcType=api&svcCode=SCHOOL&contentType=json&thisPage=1&perPage=1";
            requestUrl += "&gubun="+gubun+"&searchSchulNm=" + URLEncoder.encode(school, "UTF-8");
            URL url = new URL(requestUrl);
            URLConnection conn = url.openConnection();
            bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String jsonStr = bufferedReader.readLine();

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonStr);
            JSONObject dataSearch = (JSONObject) jsonObject.get("dataSearch");
            JSONArray content = (JSONArray) dataSearch.get("content");

            if ( content.size() == 0 ) {
                return false;
            }

            JSONObject schoolObject = (JSONObject) content.get(0);

            if (schoolObject.get("schoolName").toString().matches(".*"+school)) {
                returnBoolen = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnBoolen;
    }
}
