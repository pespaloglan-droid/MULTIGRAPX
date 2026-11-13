from pathlib import Path
from PIL import Image, ImageDraw
import random, math, json
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'app/src/main/assets'
(BASE/'art').mkdir(parents=True,exist_ok=True)
(BASE/'thumb').mkdir(exist_ok=True)
W,H=4320,7680
entries=[
('gold-orbit','Money','Gold Orbit','Siyah boşlukta metalik altın halkalar ve dengeli negatif alan.','orbit'),
('wealth-wave','Money','Liquid Assets','Siyah fonda altın akış çizgileri, soyut finansal enerji.','wave'),
('rose-signal','Love','Rose Signal','Karanlık zeminde iç içe pembe kalpler, minimal romantik kompozisyon.','heart'),
('together','Love','Two Worlds','Siyah uzayda kesişen pembe yörüngeler, birliktelik teması.','orbit'),
('midnight-suite','Luxury Life','Midnight Suite','Gece mimarisinde altın ışıklı kemerler, lüks ve sessiz bir mekân.','arch'),
('platinum','Luxury Life','Platinum Hours','Koyu zeminde zarif platin halkalar, premium minimalizm.','orbit'),
('obsidian','Black','Obsidian','Siyah ve antrasit geometrik yüzeyler, monokrom derinlik.','facet'),
('noir-flow','Black','Noir Flow','Koyu gri dalgalı ince çizgiler, siyah minimal kompozisyon.','wave'),
('last-light','Lonely','Last Light','Siyah gökyüzünde tek ay, uzak dağ siluetleri ve soğuk mavi ışık.','moon'),
('blue-hour','Lonely','Blue Hour','Uzak bir ay altında yalnız ufuk ve mavi sisli dağlar.','moon')]
catalog=[]
for seed,(ident,cat,title,prompt,kind) in enumerate(entries):
 random.seed(seed);im=Image.new('RGB',(W,H),(5,5,7));d=ImageDraw.Draw(im)
 colors={'Money':(202,166,91),'Love':(215,111,150),'Luxury Life':(203,186,150),'Black':(79,82,92),'Lonely':(106,144,186)}
 c=(192,200,215) if ident=='platinum' else colors[cat]
 def col(v):return tuple(int(x*v) for x in c)
 if kind=='orbit':
  cx,cy=W//2,int(H*.47)
  for j in range(30):
   rr=800+j*32;d.ellipse((cx-rr,cy-rr*1.46,cx+rr,cy+rr*1.46),outline=col(.15+.8*(j/30)**3),width=4+j//7)
  for j in range(13):
   rr=430+j*28;d.ellipse((cx-rr+450,cy-rr*1.4+580,cx+rr+450,cy+rr*1.4+580),outline=col(.75),width=5)
 elif kind=='wave':
  for j in range(90):
   pts=[(x,int(H*.5+j*22+math.sin(x/W*math.pi*2+j*.025)*650+math.sin(x/W*math.pi+j*.04)*850)) for x in range(-100,W+100,24)]
   d.line(pts,fill=col(.2+.65*j/90),width=5)
 elif kind=='heart':
  for off in [-330,330]:
   for k in range(18):
    s=52+k*1.8;pts=[]
    for t in range(361):
     a=t*math.pi/180;xx=16*math.sin(a)**3;yy=13*math.cos(a)-5*math.cos(2*a)-2*math.cos(3*a)-math.cos(4*a)
     pts.append((W/2+off+xx*s,H*.49-yy*s))
    d.line(pts,fill=col(.25+k/24),width=6)
 elif kind=='arch':
  for k in range(20):
   l=250+k*83;right=W-l;top=H*.23+k*112;b=H*.87-k*20
   d.arc((l,top,right,top+(right-l)),180,360,fill=col(.25+k/28),width=8)
   d.line([(l,top+(right-l)/2),(l,b)],fill=col(.45),width=7)
   d.line([(right,top+(right-l)/2),(right,b)],fill=col(.45),width=7)
  for i in range(13):d.line([(W/2,H*.63),(i*W/12,H)],fill=col(.16),width=5)
 elif kind=='facet':
  for j in range(22):
   x=random.randint(-200,W);y=random.randint(800,H);s=random.randint(350,1600)
   d.polygon([(x,y-s),(x+s,y+s//2),(x-s//2,y+s)],fill=(12+j%6*4,13+j%6*4,16+j%6*4))
   d.line([(x,y-s),(x+s,y+s//2)],fill=col(.5),width=5)
 elif kind=='moon':
  cx=int(W*(.65 if seed==8 else .35));cy=int(H*.31);rr=230
  for j in range(30,0,-1):
   rr2=rr+j*13;d.ellipse((cx-rr2,cy-rr2,cx+rr2,cy+rr2),fill=col(.015+(30-j)*.0009))
  d.ellipse((cx-rr,cy-rr,cx+rr,cy+rr),fill=col(.95));d.ellipse((cx-rr+100,cy-rr-40,cx+rr+100,cy+rr-40),fill=(5,5,7))
  for layer in range(5):
   y=int(H*(.59+layer*.07));pts=[(0,H),(0,y)]+[(x,y+random.randint(-350,350)) for x in range(0,W+300,300)]+[(W,H)]
   d.polygon(pts,fill=tuple(int(v*(.11-layer*.018)) for v in c))
 if cat!='Black':
  for k in range(65):
   x=random.randrange(W);y=random.randrange(int(H*.85));rr=random.choice([2,3,4]);d.ellipse((x-rr,y-rr,x+rr,y+rr),fill=col(random.uniform(.12,.5)))
 im.save(BASE/'art'/f'{ident}.jpg',quality=94,optimize=True,subsampling=0)
 im.resize((360,640),Image.Resampling.LANCZOS).save(BASE/'thumb'/f'{ident}.jpg',quality=88)
 catalog.append({'id':ident,'category':cat,'title':title,'prompt':prompt+' Dikey 9:16, yazısız, özgün dijital sanat.','width':W,'height':H,'type':'Procedural original artwork'})
 del im
(BASE/'catalog.json').write_text(json.dumps(catalog,ensure_ascii=False,indent=2),encoding='utf-8')
icon=Image.new('RGB',(192,192),(8,8,8));di=ImageDraw.Draw(icon)
di.rounded_rectangle((8,8,183,183),radius=45,outline=(220,194,138),width=4)
di.line([(42,135),(42,59),(95,112),(149,59),(149,135)],fill=(220,194,138),width=14)
p=ROOT/'app/src/main/res/mipmap-xxxhdpi';p.mkdir(parents=True,exist_ok=True);icon.save(p/'ic_launcher.png')
print('Generated 10 native 4320x7680 digital artworks')
