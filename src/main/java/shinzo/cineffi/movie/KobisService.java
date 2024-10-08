package shinzo.cineffi.movie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;

@Slf4j
@Service
public class KobisService {
    @Value("${kobis.api_key1}")
    private String KOBIS_API_KEY1;
    @Value("${kobis.api_key2}")
    private String KOBIS_API_KEY2;
    @Value("${kobis.api_key3}")
    private String KOBIS_API_KEY3;
    @Value("${kobis.api_key4}")
    private String KOBIS_API_KEY4;
    @Value("${kobis.api_key5}")
    private String KOBIS_API_KEY5;
    @Value("${kobis.api_key6}")
    private String KOBIS_API_KEY6;
    @Value("${kobis.api_key7}")
    private String KOBIS_API_KEY7;
    @Value("${kobis.api_key8}")
    private String KOBIS_API_KEY8;
    @Value("${kobis.api_key9}")
    private String KOBIS_API_KEY9;
    @Value("${kobis.api_key10}")
    private String KOBIS_API_KEY10;
    @Value("${kobis.api_key11}")
    private String KOBIS_API_KEY11;
    @Value("${kobis.api_key12}")
    private String KOBIS_API_KEY12;
    @Value("${kobis.api_key1}")
    public String curKobisKey;

    public void nextKobisKey(){
        if(curKobisKey.isEmpty()) {
            curKobisKey = KOBIS_API_KEY1;
        }
        else {
            String[] keys = new String[]{KOBIS_API_KEY1, KOBIS_API_KEY2, KOBIS_API_KEY3, KOBIS_API_KEY4, KOBIS_API_KEY5, KOBIS_API_KEY6, KOBIS_API_KEY7, KOBIS_API_KEY8, KOBIS_API_KEY9, KOBIS_API_KEY10, KOBIS_API_KEY11, KOBIS_API_KEY12};

            for (int i = 0; i < keys.length; i++) {
                if (i == keys.length - 1) {
                    log.error(":::::::::::::: requestKobisDatas 로그: 모든 코비스 키 만료!");
                    throw new CustomException(ErrorMsg.FAILED_TO_KOBIS_INIT_PROCESS);
                }

                if (keys[i].equals(curKobisKey)) {
                    curKobisKey = keys[i + 1];
                    break;
                }
            }
        }

    }


}
