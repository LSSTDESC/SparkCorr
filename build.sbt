/*
 * Copyright 2018 AstroLab Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import Dependencies._


lazy val root = (project in file(".")).
 settings(
   inThisBuild(List(
     version      := "1.0"
   )),
   organization := "com.github.astrolabsoftware",
   name := "SparkCorr",
   scalaVersion := "2.11.12",
   libraryDependencies ++= Seq(
     "org.apache.spark" %% "spark-core" % "2.4.5" % "provided",
     "org.apache.spark" %% "spark-sql" % "2.4.5" % "provided",
     scalaTest % Test
   )
 )

developers := List(
 Developer(
   "StephanePlaszczynski",
   "Stephane Plaszczynski",
   "plaszczy@lal.in2p3.fr",
   url("https://github.com/plaszczy")
 )
)


licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
