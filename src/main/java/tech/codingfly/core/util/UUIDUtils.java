package tech.codingfly.core.util;

import java.util.UUID;

/**
 * 封装UUID转base64的方法
 */
public abstract class UUIDUtils {

    private static final char[] BASE64 = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '-', '_'
    };
    
    public static void main(String[] args) {
        System.out.println(UU64(Long.MAX_VALUE, Long.MAX_VALUE));
        for (int i = 0; i < 10; i++) {
            while (true) {
                String base64Id = UU64();
                if (!base64Id.contains("_") && !base64Id.contains("-")) {
                    System.out.println(base64Id);
                    break;
                }
            }
        }
    }

    /**
     * @return 64进制表示的紧凑格式的 UUID
     */
    public static String UU64() {
        return UU64(UUID.randomUUID());
    }

    /**
     * 返回一个 UUID ，并用 64 进制转换成紧凑形式的字符串，内容为 [\\-0-9a-zA-Z_]
     * <p>
     * 比如一个类似下面的 UUID:
     * <p>
     * <pre>
     * a6c5c51c-689c-4525-9bcd-c14c1e107c80
     * 一共 128 位，分做L64 和 R64，分为为两个 64位数（两个 long）
     *    > L = uu.getLeastSignificantBits();
     *    > UUID = uu.getMostSignificantBits();
     * 而一个 64 进制数，是 6 位，因此我们取值的顺序是
     * 1. 从L64位取10次，每次取6位
     * 2. 从L64位取最后的4位 ＋ R64位头2位拼上
     * 3. 从R64位取10次，每次取6位
     * 4. 剩下的两位最后取
     * 这样，就能用一个 22 长度的字符串表示一个 32 长度的UUID，压缩了 1/3
     * </pre>
     * @param uu UUID 对象
     * @return 64进制表示的紧凑格式的 UUID
     */
    public static String UU64(UUID uu) {
        long left = uu.getMostSignificantBits();
        long right = uu.getLeastSignificantBits();
        return UU64(left, right);
    }

    public static String UU64(long left, long right) {
        int index = 0;
        char[] cs = new char[22];
        long mask = 63;
        // 从L64位取10次，每次取6位
        for (int off = 58; off >= 4; off -= 6) {
            long hex = (left & (mask << off)) >>> off;
            cs[index++] = BASE64[(int) hex];
        }
        // 从L64位取最后的4位 ＋ R64位头2位拼上
        int l = (int) (((left & 0xF) << 2) | ((right & (3 << 62)) >>> 62));
        cs[index++] = BASE64[l];
        // 从R64位取10次，每次取6位
        for (int off = 56; off >= 2; off -= 6) {
            long hex = (right & (mask << off)) >>> off;
            cs[index++] = BASE64[(int) hex];
        }
        // 剩下的两位最后取
        cs[index++] = BASE64[(int) (right & 3)];
        // 返回字符串
        return new String(cs);
    }

}