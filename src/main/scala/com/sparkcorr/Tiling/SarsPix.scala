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
package com.sparkcorr.Tiling

import com.sparkcorr.Geometry.{Point,Point3D,arr2}

import scala.math.{Pi,sqrt,cos,sin,acos}

import org.apache.log4j.{Level, Logger}

import java.io._
import java.util.Locale


class SarsPix(nside:Int) extends CubedSphere(nside) {


  require(nside%2==0,"nside for SARS must be even")

  //Nodes construction
  override  def buildNodes():Array[arr2[Point3D]]={

    val nodes=new Array[arr2[Point3D]](6)

    //face0
    val FACE0=new arr2[Point3D](N+1)

    //subfaces (quadrants)
    val q0:Array[arr2[Point3D]]=
      Array(subface0(1,1),subface0(1,-1),subface0(-1,1),subface0(-1,-1))
    //concatenate subsfaces
      for (ii <- 0 to N) {
        for (jj <- 0 to N) {
          val (ff,i,j)=subfaceIndex(ii,jj)
          FACE0(ii,jj)=q0(ff)(i,j)
          //println(s"FACE0 ($ii $jj) = ${FACE0(ii,jj)}")
        }
      }

    nodes(0)=FACE0
    //now build all other faces
    nodes(1)=rotateFace0(FACE0,1)
    nodes(2)=rotateFace0(FACE0,2)
    nodes(3)=rotateFace0(FACE0,3)
    nodes(4)=rotateFace0(FACE0,4)
    nodes(5)=rotateFace0(FACE0,5)

    nodes
  }

    //build sufaces for face 0
  def subface0(signa:Int,signb:Int):arr2[Point3D] = {
    val M=new arr2[Point3D](N/2+1)
    val n=N/2
    //println(s"\ncall to subface0 (signs=$signa,$signb) n=$n")
    M(0,0)=new Point3D(Pi/2,0.0)

    for (i <- 1 to N/2){
      val gi:Double=Pi/12*(i.toDouble/n)*(i.toDouble/n)+Pi/4
      val alpha_i:Double=signa*acos(sqrt(2.0)*cos(gi))
      //println(s"i=$i, gi=$gi, ai=$alpha_i")
      M(i,0)=new Point3D(Pi/2,alpha_i)
      val beta_ii:Double=signb*acos(1/(sqrt(2.0)*sin(gi)))
      M(i,i)=new Point3D(Pi/2-beta_ii,alpha_i)
      for (j <- 0 to i) {
        val beta_ij:Double=j*beta_ii/i
        M(i,j)=new Point3D(Pi/2-beta_ij,alpha_i)
        //println(s"subface0 (signs=$signa,$signb) = [$i,$j]=${M(i,j)}")
      }
    }
    //symetrize
    for (i <- 1 to N/2){
        for (j <- 0 until i){
            M(j,i)=new Point3D(M(i,j).x,signa*signb*M(i,j).z,signa*signb*M(i,j).y)
        }
    }
    M
  }


  //get subfaceindex from face index
  //I,J = face index
  def subfaceIndex(I:Int,J:Int):(Int,Int,Int)=
    if (J<=N/2)
        if (I<=N/2) (3,N/2-I,N/2-J) else (1,I-N/2,N/2-J)
    else
        if (I<=N/2) (2,N/2-I,J-N/2) else (0,I-N/2,J-N/2)

  //rotates face0 onto fnum
  def rotateFace0(face0:arr2[Point3D],fnum:Int):arr2[Point3D]={

    //define rotations on X,Y,Z from face 0 to any up to 5
    val rotations=new Array[(Double,Double,Double)=>(Double,Double,Double)](6)
    rotations(0)=(x,y,z)=>(x,y,z)
    rotations(1)=(x,y,z)=>(-y,x,z)
    rotations(2)=(x,y,z)=>(-x,-y,z)
    rotations(3)=(x,y,z)=>(y,-x,z)
    rotations(4)=(x,y,z)=>(z,y,-x)
    rotations(5)=(x,y,z)=>(-z,y,x)

    val face=new arr2[Point3D](face0.size)
    val rot=rotations(fnum)
    for ( i <- 0 until face0.size) 
    {
      for ( j <- 0 until face0.size)
      {
        val p:Point3D=face0(i,j)
        val (x,y,z)=rot(p.x,p.y,p.z)
        face(i,j)=new Point3D(x,y,z)
      }
    }
    face
  }

  override def ang2pix(theta:Double,phi:Double):Int = {
    val face:Int=getFace(theta,phi)

    val p=new Point3D(theta,phi)
    //subsface
    // subface index i,j

    //back to face index
    val (ii,jj)=(0,0)
    coord2pix(face,ii,jj)
  }




}



// companion
object SarsPix {

  def main(args:Array[String]):Unit= {
    Locale.setDefault(Locale.US)

    if (args.size!=1){
      println("*****************************************")
      println(">>>> Usage: SarsPix nside")
      println("*****************************************")
      return
    }

    val tiling=new SarsPix(args(0).toInt)

    tiling.writeCenters("centers.txt")

  }



}

