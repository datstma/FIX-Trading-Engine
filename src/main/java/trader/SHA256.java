/* @author Stefan Månsby */

package trader;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Utility class for SHA-256 hashing operations.
 * Thread-safe implementation with optimized hex conversion and input handling.
 *
 * @author Stefan Månsby
 */
public class SHA256 {
    private static final int HEX_CHARS_LENGTH = 2;
    private static final String ALGORITHM = "SHA-256";
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    // ThreadLocal for thread-safe MessageDigest reuse
    private static final ThreadLocal<MessageDigest> MESSAGE_DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize SHA-256 algorithm", e);
        }
    });

    /**
     * Computes SHA-256 hash of the input string.
     *
     * @param input the string to hash
     * @return hexadecimal representation of the hash
     * @throws IllegalArgumentException if input is null
     */
    public static String getHash(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        return hashBytes(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Computes SHA-256 hash of the input byte array.
     *
     * @param input the byte array to hash
     * @return hexadecimal representation of the hash
     * @throws IllegalArgumentException if input is null
     */
    public static String hashBytes(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        MessageDigest digest = MESSAGE_DIGEST.get();
        digest.reset(); // Reset for reuse
        byte[] hashBytes = digest.digest(input);
        return bytesToHex(hashBytes);
    }

    /**
     * Computes SHA-256 hash of a large byte array using buffered processing.
     *
     * @param input the byte array to hash
     * @param bufferSize the size of the buffer to use
     * @return hexadecimal representation of the hash
     * @throws IllegalArgumentException if input is null or bufferSize is less than 1
     */
    public static String hashLargeByteArray(byte[] input, int bufferSize) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        if (bufferSize < 1) {
            throw new IllegalArgumentException("Buffer size must be positive");
        }

        MessageDigest digest = MESSAGE_DIGEST.get();
        digest.reset(); // Reset for reuse

        for (int i = 0; i < input.length; i += bufferSize) {
            int end = Math.min(i + bufferSize, input.length);
            digest.update(input, i, end - i);
        }

        return bytesToHex(digest.digest());
    }

    /**
     * Converts a byte array to its hexadecimal representation.
     * This implementation uses a pre-calculated hex char array and
     * proper initial capacity StringBuilder for better performance.
     *
     * @param bytes the byte array to convert
     * @return hexadecimal string representation
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * HEX_CHARS_LENGTH];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Verifies if a string matches a given SHA-256 hash.
     *
     * @param input the string to verify
     * @param hash the expected hash
     * @return true if the input's hash matches the expected hash
     * @throws IllegalArgumentException if either input or hash is null
     */
    public static boolean verifyHash(String input, String hash) {
        if (input == null || hash == null) {
            throw new IllegalArgumentException("Input and hash cannot be null");
        }
        String computedHash = getHash(input);
        return MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                hash.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Combines multiple byte arrays and computes their SHA-256 hash.
     *
     * @param arrays the byte arrays to combine and hash
     * @return hexadecimal representation of the combined hash
     * @throws IllegalArgumentException if arrays is null or empty
     */
    public static String hashMultipleArrays(byte[]... arrays) {
        if (arrays == null || arrays.length == 0) {
            throw new IllegalArgumentException("At least one byte array must be provided");
        }

        MessageDigest digest = MESSAGE_DIGEST.get();
        digest.reset();

        for (byte[] array : arrays) {
            if (array != null) {
                digest.update(array);
            }
        }

        return bytesToHex(digest.digest());
    }
}