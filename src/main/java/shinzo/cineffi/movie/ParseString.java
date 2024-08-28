package shinzo.cineffi.movie;

import org.springframework.stereotype.Service;

@Service
public class ParseString {
    public String makeNoBlankStr(String blankStr){
        if(blankStr == null) return "";
        String result = "";
        for (int i = 0; i < blankStr.length(); i++) {
            if(blankStr.charAt(i) != ' ') result += blankStr.charAt(i);
        }
        return result;
    }
}
