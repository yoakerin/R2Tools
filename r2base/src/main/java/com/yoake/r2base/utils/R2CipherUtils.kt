package com.yoake.r2base.utils
import android.util.Base64
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 加密和解密相关的工具
 */
object R2CipherUtils {
    //base64数据使用
    const val CBC_PADDING_NO = "NoPadding"

    //十六进制数据使用
    const val CBC_PADDING_PKCS5 = "PKCS5Padding"

    @JvmStatic
    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(input.toByteArray())
        val no = BigInteger(1, messageDigest)
        var hashText = no.toString(16)
        while (hashText.length < 32) {
            hashText = "0$hashText"
        }
        return hashText
    }

    /**
     * 将十六进制字符串转换为字节数组
     */
    fun hexStringToByteArray(hexString: String): ByteArray {
        val hex = hexString.replace(" ", "")
        val result = ByteArray(hex.length / 2)
        for (i in hex.indices step 2) {
            val byte = hex.substring(i, i + 2).toInt(16).toByte()
            result[i / 2] = byte
        }
        return result
    }

    /**
     *
     * AES-CBC是AES（Advanced Encryption Standard）算法的一种模式，CBC代表Cipher Block Chaining。
     * 在AES-CBC模式中，每个明文块被分成固定长度的块，然后每个块都被加密。
     * 在加密下一个块之前，会将上一个块的加密结果与当前块进行异或运算，然后再进行加密。
     * 这种方式使得每个加密块都依赖于前一个加密块，增加了加密的随机性。
     * AES-CBC模式需要一个初始向量（IV），它是一个固定长度的随机数，用于在加密过程中引入更多的随机性和不可预测性。
     * 解密时，需要使用相同的IV来正确解密数据。
     */
    @JvmStatic
    fun aesCbcDecrypt(
        content: String, key: String, iv: String, padding: String = CBC_PADDING_PKCS5
    ): String {
        try {
            val keySpec = SecretKeySpec(key.toByteArray(StandardCharsets.US_ASCII), "AES")
            val cipher = Cipher.getInstance("AES/CBC/${padding}")
            val params = IvParameterSpec(iv.toByteArray())
            cipher.init(Cipher.DECRYPT_MODE, keySpec, params)
            val bytes = if (padding == CBC_PADDING_PKCS5) {
                hexStringToByteArray(content)
            } else {
                Base64.decode(content, 0)
            }
            val original = cipher.doFinal(bytes)
            return String(original,StandardCharsets.UTF_8).trim { it<= ' '}

        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    /**
     *RSA公钥加密
     */
    @JvmStatic
    @Throws(java.lang.Exception::class)
    fun rsaEncrypt(str: String, publicKey: String?): String? {
        //base64编码的公钥
        val decoded = Base64.decode(publicKey, Base64.DEFAULT)
        val pubKey = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(decoded)) as RSAPublicKey
        //RSA加密
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, pubKey)
        return Base64.encodeToString(cipher.doFinal(str.toByteArray()), Base64.DEFAULT)
    }
}