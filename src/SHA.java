
import java.security.MessageDigest;

/**
 * Created by song on 17-2-19.
 * <p>
 * SHA(Secure Hash Algorithm，安全散列算法）加密
 */
public final class SHA {

    private SHA() {
        /*do nothing*/
    }

    /**
     * 使用SHA-256加密
     *
     * @param input 输入
     * @return 加密结果
     */
    public static String encrypt(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            return toHexString(messageDigest.digest(input.getBytes("UTF8")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * byte数组转为16进制字符串
     */
    private static String toHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();

        for (byte aByte : bytes) {
            stringBuilder.append(Integer.toHexString(aByte & 0xff | 0xffffff00).substring(6));
        }

        return stringBuilder.toString();
    }
}
