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
import com.sparkcorr.Geometry.{Point3D,arr2}

import org.scalatest.{BeforeAndAfter, FunSuite}
import scala.math.{Pi,abs,acos,sqrt,toRadians,toDegrees}
import scala.util.Random
import java.util.Locale

/**
  * Test class for SARSPix
  */
class SARSPixTest extends FunSuite with BeforeAndAfter {

  var c: SARSPix = _
  val N:Int= 10

  before {
    Locale.setDefault(Locale.US)
    c= new SARSPix(N)
  }

  test(s"construct object of size $N Npix=${6*N*N/1000000.0} M") {
    assert(true)
  }

   test("Numbers of pixels shoudl be 6N^2"){
    assert(c.pixNums.size==6*N*N)
  }


  test("Valid pixel numbers"){
    for (ipix<-c.pixNums) {
      assert(c.isValidPix(ipix)==true)
    }
  }
 

  test("Pixnum to coord") {
    for (ipix<-c.pixNums) {
      val (f,i,j)=c.pix2coord(ipix)
      assert(c.coord2pix(f,i,j)==ipix)
    }
  }


 test("Coord to pixnum") {
    for (f<-0 to 5; i<-0 until N; j<- 0 until N) {
      val ipix=c.coord2pix(f,i,j)
      assert(c.pix2coord(ipix)==(f,i,j))
    }
  }


  test("pix2ang returns angles in the correct range") {
    for (ipix<-c.pixNums) {
      val Array(theta,phi)=c.pix2ang(ipix)
      assert(theta>=0 & theta<=Pi & phi>=0 & phi<=2*Pi,f"theta=$theta phi=$phi")
    }
  }


  test("face2localIndex + local2faceIndex") {
    for (I <- 0 until c.N) {
      for (J <- 0 until c.N) {
        val (q,i,j)=c.face2localIndex(I,J)
        assert(c.local2faceIndex(q,i,j)==(I,J))
      }
    }
  }

  test("test all indices") {
     for (ipix<-c.pixNums) {
       val (f,ii,jj)=c.pix2coord(ipix)
       val (q,i,j)=c.face2localIndex(ii,jj)

       val (ib,jb)=c.local2faceIndex(q,i,j)
       assert(ib==ii & jb==jj)

       assert(c.coord2pix(f,ib,jb)==ipix)

     }
  
}


  test("Faces and quadrants") {
 
    //azimuthal
    for (f <- 0 to 3) {
      val f0=f*Pi/2
      val t0=Pi/2
      assert(c.getFaceQuadrant(t0-Pi/8,f0+Pi/8)==(f,0))
      assert(c.getFaceQuadrant(t0+Pi/8,f0+Pi/8)==(f,1))
      assert(c.getFaceQuadrant(t0-Pi/8,(f0-Pi/8+2*Pi)%(2*Pi))==(f,2))
      assert(c.getFaceQuadrant(t0+Pi/8,(f0-Pi/8+2*Pi)%(2*Pi))==(f,3))
    }
    //upper face (4)
    assert(c.getFaceQuadrant(Pi/8,Pi/4)==(4,1))
    assert(c.getFaceQuadrant(Pi/8,Pi/4+Pi/2)==(4,0))
    assert(c.getFaceQuadrant(Pi/8,Pi/4+Pi)==(4,2))
    assert(c.getFaceQuadrant(Pi/8,Pi/4+3*Pi/2)==(4,3))

    //bottom face (5)
    assert(c.getFaceQuadrant(Pi-Pi/8,Pi/4)==(5,0))
    assert(c.getFaceQuadrant(Pi-Pi/8,Pi/4+Pi/2)==(5,1))
    assert(c.getFaceQuadrant(Pi-Pi/8,Pi/4+Pi)==(5,3))
    assert(c.getFaceQuadrant(Pi-Pi/8,Pi/4+3*Pi/2)==(5,2))

  }
 
  test("FACE0 test pixel index from nodes"){
    /*
    val ai=10.0
    val bi=30.0
    val index=(0,1)
    val (f,q,i,j)=c.getLocalIndex(new Point3D(Pi/2-toRadians(bi),toRadians(ai)))
    assert(f==0 & q==0 & (i,j)==index,s"\n alpha=$ai deg beta=$bi deg=> f=$f q=$q i=$i j=$j")
     */

    //shift y-z slightly yo vaoid numerical issues
    val eps=1.0/(c.N*100)

    val s=Map(0->(1,1),1->(1,-1),2->(-1,1),3->(-1,-1))
    for (q <- 0 to 3) {
    val (signy,signz)=s(q)
      val quad:arr2[Point3D]=c.newF0Quadrant(q)
      for (i <- 1 to quad.size-2; j <- 1 to quad.size-2){
        val node=quad(i,j)
        val ps=new Point3D(node.x,node.y-signy*eps,node.z-signz*eps)
        val p3=new Point3D(ps/ps.norm)
        val loc=c.getLocalIndex(p3)
        assert(loc==(0,q,i-1,j-1),s"\ni=$i j=$j back=$loc $p3")
      }
    }
  }

  test("FACE1 test pixel index from nodes"){
    val nodes:Array[arr2[Point3D]]=c.buildNodes

    val numFace=1
    val eps=1.0/(c.N*100)

    val FACE:arr2[Point3D]=nodes(numFace)
    for (I <- 1 until FACE.size; J <- 1 until FACE.size){
      val node=FACE(I,J)
      val (q,i,j)=c.face2localIndex(I,J)
      //avoid borders
      if (j>0 & i>0 & i<c.N/2.0-1 & j<c.N/2.0-1) {
        val s=Map(0->(1,1),1->(1,-1),2->(-1,1),3->(-1,-1))
        val (signy,signz)=s(q)
        //pas un bon smearing hors face0
        val ps=new Point3D(node.x,node.y-signy*eps,node.z-signz*eps)
        val p3=new Point3D(ps/ps.norm)
        val loc=c.getLocalIndex(p3)
        assert(loc==(numFace,q,i-1,j-1),s"\ni=${i-1} j=${j-1} back=$loc $p3")
      }
    }

  }


  test("Pix2Ang+Ang2pix "){
      for (ipix<-c.pixNums) {
        val (f,ii,jj)=c.pix2coord(ipix)
        val (q,i,j)=c.face2localIndex(ii,jj)

        val Array(theta,phi)=c.pix2ang(ipix)
        val iback=c.ang2pix(theta,phi)

        val (fb,iib,jjb)=c.pix2coord(iback)
        val (qb,ib,jb)=c.face2localIndex(iib,jjb)

        assert(ipix==ib,s"\ninput: ipix=$ipix face=($f,$ii,$jj) local=($q,$i,$j)  output: ipix=$iback face=($fb,$iib,$jjb) local=($qb,$ib,$jb)  output")
      }
    
  }
  /*
  test("Pixels max radius") {

    //theoretical values for square
    val Asq=4*Pi/(6*N*N)
    val Rmax=1.25*sqrt(Asq/2)

    val Ntot=1000000
    //random angles
    val angles=Seq.fill(Ntot)((acos(2*Random.nextDouble-1),2*Pi*Random.nextDouble))

    for ((t,f) <- angles) {
      val ipix=c.ang2pix(t,f)
      val Array(tc,fc)=c.pix2ang(ipix)
      val p=new Point3D(t,f)
      val cen=new Point3D(tc,fc)
      val r=cen.dist(p)
      assert(r<Rmax,s"\n ipix=$ipix theta=$t phi=$f r=$r")
    }
  }
   */
 

  test("Neighbours"){
    //theoretical values for square
    val Asq=4*Pi/(6*N*N)
    val Rsq=sqrt(Asq/2)
    val Rmin=Rsq
    val Rmax=1.1*Rsq

    
    for (ipix<-c.pixNums) {
      val id=c.pix2coord(ipix)
      val Array(tc,phic)=c.pix2ang(ipix)
      val pcen=new Point3D(tc,phic)

      val n=c.neighbours(ipix)
      for (in <- n) {
        val (f,i,j)=c.pix2coord(in)
        val ang=c.pix2ang(in)
        val p=new Point3D(ang(0),ang(1))
        val r=pcen.dist(p)
        assert(r>1.2*Rmin & r<2*Rmax,s"\n ipix=$ipix$id ang=($tc,$phic) voisins=$n : $pcen=pcen -> fail on $in=($f,$i,$j) angle=${ang(0)},${ang(1)} p=$p")
      }

    } //pixnum



  } //test


}
