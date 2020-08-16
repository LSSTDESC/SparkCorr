/*
 * Copyright 2020 AstroLab Software
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
package com.astrolabsoftware.sparkcorr.IO

import scala.io.Source
import collection.mutable.Map

class ParamFile(f:String) {
  val m=ParamFile.parsefile(f)

  def contains(key:String) = m.contains(key)
  override def toString = m.mkString("\n")

  def findType[T](key:String,dflt:T,conv: String=>T):T= if (m.contains(key)) conv(m(key)) else dflt

  def find(key:String,dflt:String):String = m.getOrElse(key,dflt).toString
  def find(key:String,dflt:Short):Short = findType[Short](key,dflt,_.toShort)
  def find(key:String,dflt:Int):Int = findType[Int](key,dflt,_.toInt)
  def find(key:String,dflt:Long):Long = findType[Long](key,dflt,_.toLong)
  def find(key:String,dflt:Float):Float = findType[Float](key,dflt,_.toFloat)
  def find(key:String,dflt:Double):Double = findType[Double](key,dflt,_.toDouble)

}

//companion
object ParamFile {

  def parsefile(f:String):Map[String,String]={
    val m=Map.empty[String,String]
    for (
      line <-Source.fromFile(f).getLines()
      if !line.startsWith("#")
      if (line.count(_ == '=') == 1)
        ) {
      val a=line.split("=").map(_.trim)
      m(a(0))=a(1)
    }
    m
  }

  def main(args:Array[String])= {
    require(args.size==1)
    val params=new ParamFile(args(0))
    println(params)
  }

}

