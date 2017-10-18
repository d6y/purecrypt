package secret

import org.scalacheck.{Properties}
import org.scalacheck.Prop.forAll
import scala.util.Success

object SecretSpecification extends Properties("Crypt") {
   val c = Crypt()
   val key = {
     import javax.crypto.KeyGenerator
     val keySize = 128
     val gen = KeyGenerator.getInstance("AES")
     gen.init(keySize)
     gen.generateKey()
   }

  import javax.crypto.Cipher
  val mkCipher = () => Cipher.getInstance("AES", "BC")

  property("end-to-end") = forAll { (msg: String) =>
   val m = Bytes[Encoding.Unencrypted](msg.getBytes("UTF-8"))
   val o = for {
     e <- c.encrypt(mkCipher)(key, m)
     d <- c.decrypt(mkCipher)(key, e)
   } yield d
   o == Success(m)
  }

}
