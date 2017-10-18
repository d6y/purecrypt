import Dependencies._

lazy val root = (project in file(".")).
  settings(
    organization  := "com.dallaway",
    scalaVersion  := "2.12.3",
    version       := "1.0.0",
    name          := "secret",
    scalacOptions := Recommended.lightbendScalacOptions,
    libraryDependencies ++= Seq(bouncyCastle, scalaCheck),
  )
