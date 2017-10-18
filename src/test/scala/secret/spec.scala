package secret

import org.scalacheck.{Properties}
import org.scalacheck.Prop.forAll
import scala.util.Success

object SecretSpecification extends Properties("Crypt") {
   val c = Crypt()
   val key = c.newKey

  property("end-to-end") = forAll { (msg: String) =>
   val m = ClearText(msg)
   val o = for {
     k <- key
     e <- c.encrypt(k, m)
     d <- c.decrypt(k, e)
   } yield d
   o == Success(m)
  }

}
