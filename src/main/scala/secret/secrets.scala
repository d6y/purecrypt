package secret

import scala.util.Try

/*
Example of encrypting text string.

Useful resources:
-  https://stackoverflow.com/questions/1220751/how-to-choose-an-aes-encryption-mode-cbc-ecb-ctr-ocb-cfb
*/

final case class ClearText(value: String) extends AnyVal
final case class Base64Key(value: String) extends AnyVal
final case class CipherText(value: String) extends AnyVal

case class Crypt() {

  import javax.crypto.{KeyGenerator, SecretKey, Cipher}
  import javax.crypto.spec.SecretKeySpec
  import java.util.Base64
  import java.nio.ByteBuffer
  import java.nio.charset.StandardCharsets.UTF_8

  lazy val b64encoder: Array[Byte] => String = Base64.getEncoder().encodeToString _
  lazy val b64decoder: String => Array[Byte] = Base64.getDecoder().decode _

  def newKey: Try[Base64Key] = Try {
    val keySize = 128
    val gen = KeyGenerator.getInstance("AES")
    gen.init(keySize)
    val keyBytes = gen.generateKey().getEncoded()
    Base64Key(b64encoder(keyBytes))
  }

  // Running some bytes through a cipher
  private def run(cipher: Cipher, input: Array[Byte]): Array[Byte] = {
    val output = ByteBuffer.allocate(cipher.getOutputSize(input.length))
    val numBytesWritten = cipher.doFinal(ByteBuffer.wrap(input), output)
    output.array().take(numBytesWritten)
  }

  def encrypt(key: Base64Key, text: ClearText): Try[CipherText] = Try {
    val secretKey: SecretKey = new SecretKeySpec(b64decoder(key.value), "AES")

    val cipher = Cipher.getInstance("AES", "BC")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)

    val output = run(cipher, text.value.getBytes(UTF_8))
    CipherText(b64encoder(output))
  }

  def decrypt(key: Base64Key, text: CipherText): Try[ClearText] = Try {
    val secretKey: SecretKey = new SecretKeySpec(b64decoder(key.value), "AES")

    val cipher = Cipher.getInstance("AES", "BC")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)

    val output = run(cipher, b64decoder(text.value))
    ClearText(new String(output, UTF_8))
  }

}

object Example {
  def main(args: Array[String]): Unit = {

    import org.bouncycastle.jce.provider.BouncyCastleProvider
    import java.security.Security
    Security.addProvider(new BouncyCastleProvider())

    val c = Crypt()
    val in = ClearText("Hello")

    val out = for {
      key <- c.newKey
      _    = println(key.value)
      e   <- c.encrypt(key, in)
      d   <- c.decrypt(key, e)
    } yield d

    println(out)
  }
}
