#!/usr/bin/env python3
"""Convert the checked-in MVP contour SVGs into Android VectorDrawables. No dependencies."""
from pathlib import Path
import re,xml.etree.ElementTree as ET
R=Path(__file__).resolve().parent.parent
NS='http://schemas.android.com/apk/res/android'
ET.register_namespace('android',NS)
def attrs(**values):return {'{'+NS+'}'+k:str(v) for k,v in values.items()}
def color(value):
 if value in ('none','transparent'):return '#00000000'
 if value in ('currentColor','#000','black'):return '#FF000000'
 if value in ('white','#fff','#FFF'):return '#FFFFFFFF'
 return value

def convert(source,target,adaptive=False):
 svg=ET.parse(source).getroot();view=svg.attrib['viewBox'].split();size=int(float(view[2]))
 root=ET.Element('vector',attrs(width=f'{size}dp',height=f'{size}dp',viewportWidth=view[2],viewportHeight=view[3]))
 parent=root
 if adaptive:parent=ET.SubElement(root,'group',attrs(translateX=18,translateY=18,scaleX=2/3,scaleY=2/3))
 def visit(node,dest,inherited):
  tag=node.tag.split('}')[-1]
  if tag in ('title','desc'):return
  style={**inherited,**node.attrib}
  if tag in ('svg','g'):
   if tag=='g':
    transform=node.get('transform','');args={}
    for kind,values in re.findall(r'(translate|scale)\(([^)]+)\)',transform):
     vals=re.split(r'[ ,]+',values.strip());x=vals[0];y=vals[1] if len(vals)>1 else ('0' if kind=='translate' else x)
     args.update({kind+'X':x,kind+'Y':y})
    dest=ET.SubElement(dest,'group',attrs(**args))
   for child in node:visit(child,dest,style)
   return
  if tag=='path':path=node.attrib['d']
  elif tag=='circle':
   x,y,r=(float(node.get(k)) for k in ('cx','cy','r'))
   path=f'M{x-r},{y} A{r},{r} 0 1,0 {x+r},{y} A{r},{r} 0 1,0 {x-r},{y} Z'
  elif tag=='rect':
   x,y,w,h,rx=(float(node.get(k,'0')) for k in ('x','y','width','height','rx'))
   path=f'M{x+rx},{y} H{x+w-rx} Q{x+w},{y} {x+w},{y+rx} V{y+h-rx} Q{x+w},{y+h} {x+w-rx},{y+h} H{x+rx} Q{x},{y+h} {x},{y+h-rx} V{y+rx} Q{x},{y} {x+rx},{y} Z'
  else:raise ValueError(tag)
  values={'pathData':path,'fillColor':color(style.get('fill','none'))}
  if style.get('stroke','none')!='none':values.update(strokeColor=color(style['stroke']),strokeWidth=style.get('stroke-width','1'),strokeLineCap=style.get('stroke-linecap','butt'),strokeLineJoin=style.get('stroke-linejoin','miter'))
  ET.SubElement(dest,'path',attrs(**values))
 visit(svg,parent,{})
 ET.indent(root,space='    ');target.write_text('<?xml version="1.0" encoding="utf-8"?>\n'+ET.tostring(root,encoding='unicode')+'\n')

for source in (R/'assets/dice').glob('*.svg'):
 adaptive=source.stem=='app-icon'
 target=R/'app/src/main/res/drawable'/('ic_launcher_foreground.xml' if adaptive else f'die_{source.stem}.xml')
 convert(source,target,adaptive)
print('Converted 7 dice/Coin icons and the launcher foreground; SVG strokes retained.')
