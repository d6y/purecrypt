package secret

import scala.util.Try

/*
Example of encrypting text string.

Useful resources:
-  https://stackoverflow.com/questions/1220751/how-to-choose-an-aes-encryption-mode-cbc-ecb-ctr-ocb-cfb
*/

// final case class ClearText(value: String) extends AnyVal
// final case class Base64Key(value: String) extends AnyVal
// final case class CipherText(value: String) extends AnyVal

sealed trait Encoding
object Encoding {
  sealed trait Unencrypted extends Encoding
  sealed trait Encrypted   extends Encoding
}

final case class Bytes[T <: Encoding](value: Array[Byte]) {
  lazy val length = value.length
}
  
case class Crypt() {

  import javax.crypto.{SecretKey, Cipher}
  // import javax.crypto.spec.SecretKeySpec
  import java.util.Base64
  import java.nio.ByteBuffer

  lazy val b64encoder: Array[Byte] => String = Base64.getEncoder().encodeToString _
  lazy val b64decoder: String => Array[Byte] = Base64.getDecoder().decode _

  /*
  def newKey: Try[Base64Key] = Try {
    val keySize = 128
    val gen = KeyGenerator.getInstance("AES")
    gen.init(keySize)
    val keyBytes = gen.generateKey().getEncoded()
    Base64Key(b64encoder(keyBytes))
  }*/

  import Encoding._

  val encrypt = run[Unencrypted,Encrypted](Cipher.ENCRYPT_MODE) _
  val decrypt = run[Encrypted,Unencrypted](Cipher.DECRYPT_MODE) _

  private def run[From <: Encoding, To <: Encoding](direction: Int)(mkCipher: () => Cipher): (SecretKey, Bytes[From]) => Try[Bytes[To]] =
    (key, input) => Try {
      val cipher = mkCipher()
      cipher.init(direction, key)
      val output = ByteBuffer.allocate(cipher.getOutputSize(input.length))
      val numBytesWritten = cipher.doFinal(ByteBuffer.wrap(input.value), output)
      Bytes(output.array().take(numBytesWritten))
    }
    
/*
  def encrypt(key: Base64Key, text: ClearText): Try[CipherText] = Try {
    val secretKey: SecretKey = new SecretKeySpec(b64decoder(key.value), "AES")

    val cipher = Cipher.getInstance("AES", "BC")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)

    val output = run0(cipher, text.value.getBytes(UTF_8))
    CipherText(b64encoder(output))
  }

  def decrypt(key: Base64Key, text: CipherText): Try[ClearText] = Try {
    val secretKey: SecretKey = new SecretKeySpec(b64decoder(key.value), "AES")

    val cipher = Cipher.getInstance("AES", "BC")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)

    val output = run0(cipher, b64decoder(text.value))
    ClearText(new String(output, UTF_8))
  }
 // */
}

object Example {
  def main(args: Array[String]): Unit = {

    import org.bouncycastle.jce.provider.BouncyCastleProvider
    import java.security.Security
    Security.addProvider(new BouncyCastleProvider())

    // A key:
    import javax.crypto.KeyGenerator
    val keySize = 128
    val gen = KeyGenerator.getInstance("AES")
    gen.init(keySize)
    val key = gen.generateKey()

    // A message:
    import java.nio.charset.StandardCharsets.UTF_8
    val msg = Bytes[Encoding.Unencrypted]("Hello".getBytes(UTF_8))

    // The kind of cipher we want to use:
    import javax.crypto.Cipher
    val mkCipher = () => Cipher.getInstance("AES", "BC")
    
    val c = Crypt()

  // makeCipher
  // for { 
  //   bytesO <- fromString
  //   bytesI <- enc(key, bytes)
  //   text   <- toString
  // } yield t

    val decoded = for {
      e <- c.encrypt(mkCipher)(key,msg)
      d <- c.decrypt(mkCipher)(key,e)
    } yield d

    // Turn bytes back into text
    println(
      decoded.map(bs => new String(bs.value, UTF_8))
    )

  }
}
