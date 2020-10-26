from pylab import *
from tools import *

from pylab import *

#
a=1/sqrt(3.)

#for one face
N=int(sys.argv[1])

#equal angle
la=linspace(-pi/4,pi/4,N)
xx=a*tan(la)
x,y=meshgrid(xx,xx)

normalize=lambda p: p/sqrt(p.dot(p))

#nodes
nodes=[]
for i in range(N):
    for j in range(N):
        ipix=i*N+j
        p=array([x[i,j],y[i,j],a])
        p=normalize(p)
        nodes.append(p)

def dist(p1,p2):
    return sqrt(sum((p1-p2)**2))

def dist2(p1,p2):
    return sum((p1-p2)**2)

def xyz_to_thetaphi(v):
    x,y,z=v
    phi=np.arctan2(y,x) #[0,2pi]
    t=np.arccos(z) #[0,pi]
    return [t,phi]


#cells
Air=zeros((N-1,N-1))
e=zeros((N-1,N-1))
Rmax=zeros((N-1,N-1))
Rmin=zeros((N-1,N-1))
for ip in range(N*N):
    i,j=ip//N,ip%N
    #skip borders
    if (i+1)%N==0 or (j+1)%N==0:
        continue
    A=nodes[ip]
    B=nodes[ip+1]
    C=nodes[ip+N+1]
    D=nodes[ip+N]
    
    p2=dist2(A,C)
    q2=dist2(B,D)

    a2=dist2(A,B)
    b2=dist2(B,C)
    c2=dist2(C,D)
    d2=dist2(D,A)

    Air[i,j]=sqrt(4*p2*q2-(b2+d2-a2-c2)**2)/4
#losange
    #A[i,j]=p*q/2
    e[i,j]=sqrt(p2/q2)
    cen=(A+B+C+D)/4.
    ri=array([dist(cen,A),dist(cen,B),dist(cen,C),dist(cen,B)])
    Rmax[i,j]=amax(ri)
    Rmin[i,j]=amin(ri)


Aexp=4*pi/6/(N)**2
Rexp=sqrt(Aexp/2)

imshowXY(arange(N-1),arange(N-1),Air/Aexp,vmin=0.85,vmax=1.15)
title("area")

imshowXY(arange(N-1),arange(N-1),abs(e-1),vmin=0,vmax=0.8)
title("ellipticity")

imshowXY(arange(N-1),arange(N-1),Rmax/Rexp,vmin=0.85,vmax=1.35)
title("radius")

#histo R
figure()
Rmin=Rmin.flatten()/Rexp
hist(Rmin,bins=80,range=[0.7,1.5])

Rmax=Rmax.flatten()/Rexp
hist(Rmax,bins=80,alpha=0.5,range=[0.7,1.5])
xlabel("R/Rsq")

Rin=Rmin*Rmax/sqrt(Rmin**2+Rmax**2)
hist_plot(Rin)

show()
