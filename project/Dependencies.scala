import sbt._

object Dependencies {
  lazy val bouncyCastle = "org.bouncycastle" % "bcprov-jdk15on" % "1.58"
  lazy val scalaCheck   = "org.scalacheck"  %% "scalacheck"     % "1.13.4" % "test"
}
