package ch.idsia.crema.adaptive;

public class Quaternary {
    int base = 4;
    String zeroes;

    Quaternary(int length) {
        // String of zeros as long as length
        zeroes = new String(new char[length]).replace("\0", "0");
    }

    void generate(int[][] profiles) {
        for (int i = 0; i < profiles.length; i++) {
            String s = Integer.toString(i, base);

            // pad with leading zeros if needed
            String result = s.length() < zeroes.length()
                    ? zeroes.substring(s.length()) + s : s;

            final String[] binary_arr = result.split("");
            int[] profile = new int[binary_arr.length];
            for (int j = 0; j < binary_arr.length; j++) {
                int num_int = Integer.parseInt(binary_arr[j]);
                profile[j] = num_int;
            }

            profiles[i] = profile;

        }
    }
}
