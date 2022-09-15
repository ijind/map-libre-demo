#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
reload(sys)
sys.setdefaultencoding('utf8')

import os
import re
import argparse
from io import open
import json
import copy
import random
import reportlab.rl_config
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib import fonts, colors
from reportlab.platypus import Paragraph, Table, TableStyle, Image, ListFlowable, ListItem
from reportlab.platypus import BaseDocTemplate, Frame, PageTemplate, SimpleDocTemplate
from reportlab.lib.enums import TA_JUSTIFY, TA_LEFT, TA_CENTER, TA_RIGHT
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm, cm, inch
from reportlab.platypus.doctemplate import PageBreak, Preformatted, Spacer
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.graphics.charts.piecharts import Pie
from reportlab.graphics.shapes import Drawing
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.platypus.tableofcontents import TableOfContents
from reportlab.lib.styles import ParagraphStyle as PS
from reportlab.lib.colors import CMYKColor
from reportlab.lib import utils
from reportlab.pdfbase.pdfmetrics import stringWidth
from collections import OrderedDict
from cgi import escape
import codecs

MARGIN_SIZE = 10 * mm
PAGE_SIZE = A4

styles = getSampleStyleSheet()
styleBH = styles["Normal"]
styleH2 = styles["Heading2"]
styleH3 = styles["Heading3"]
styleH4 = styles["Heading4"]
styleBL = styles["Bullet"]
styleCD = styles["Code"]

global titleStyle
titleStyle = copy.deepcopy(styleBH)
titleStyle.fontName = 'sourcehansans_title'
titleStyle.fontSize = 24
titleStyle.spaceBefore = 50
titleStyle.spaceAfter = 24
titleStyle.alignment = TA_CENTER
titleStyle.textColor = colors.royalblue

global slaveTitleStyle
slaveTitleStyle = copy.deepcopy(styleBH)
slaveTitleStyle.fontName = 'sourcehansans_title'
slaveTitleStyle.fontSize = 12
slaveTitleStyle.spaceBefore = 16
slaveTitleStyle.spaceAfter = 48
slaveTitleStyle.alignment = TA_CENTER
slaveTitleStyle.textColor = colors.royalblue

global subtitleStyle
subtitleStyle = copy.deepcopy(styleH2)
subtitleStyle.name = 'subtitleStyle'
subtitleStyle.fontName = 'sourcehansans_title'
subtitleStyle.fontSize = 12
subtitleStyle.spaceBefore = 10
subtitleStyle.spaceAfter = 10
subtitleStyle.leading = 16
subtitleStyle.firstLineIndent = 4
subtitleStyle.backColor = colors.royalblue
subtitleStyle.textColor = colors._CMYK_white

global lv3titleStyle
lv3titleStyle = copy.deepcopy(styleH3)
lv3titleStyle.name = 'lv3titleStyle'
lv3titleStyle.fontName = 'sourcehansans_title'
lv3titleStyle.fontSize = 12
lv3titleStyle.spaceBefore = 6
lv3titleStyle.spaceAfter = 6
lv3titleStyle.leading = 16
lv3titleStyle.firstLineIndent = 4
lv3titleStyle.textColor = colors.royalblue

global lv4titleStyle
lv4titleStyle = copy.deepcopy(styleH4)
lv4titleStyle.name = 'lv4titleStyle'
lv4titleStyle.fontName = 'sourcehansans_title'
lv4titleStyle.fontSize = 9
lv4titleStyle.spaceBefore = 5
lv4titleStyle.spaceAfter = 5
lv4titleStyle.leading = 16
lv4titleStyle.firstLineIndent = 4
lv4titleStyle.textColor = colors.royalblue

global linkStyle
linkStyle = copy.deepcopy(styleBH)
linkStyle.fontName = 'sourcehansans_content'
linkStyle.fontSize = 8
linkStyle.spaceBefore = 4
linkStyle.spaceAfter = 4
linkStyle.firstLineIndent = 8
linkStyle.linkUnderline = 1
linkStyle.textColor = colors.mediumblue

global archTextStyle
archTextStyle = copy.deepcopy(styleH4)
archTextStyle.fontName = 'sourcehansans_content'
archTextStyle.fontSize = 8
archTextStyle.spaceBefore = 4
archTextStyle.spaceAfter = 4
archTextStyle.firstLineIndent = 2
archTextStyle.leading = 12

global normalStyle
normalStyle = copy.deepcopy(styleBH)
normalStyle.fontName = 'sourcehansans_content'
normalStyle.fontSize = 8
normalStyle.spaceBefore = 4
normalStyle.spaceAfter = 4
normalStyle.firstLineIndent = 8
normalStyle.leading = 16

global cellStyle
cellStyle = copy.deepcopy(styleBH)
cellStyle.fontName = 'sourcehansans_content'
cellStyle.fontSize = 8
cellStyle.spaceBefore = 4
cellStyle.spaceAfter = 4
cellStyle.firstLineIndent = 4
cellStyle.leading = 10

global codeStyle
codeStyle = copy.deepcopy(styleBH)
codeStyle.fontName = 'sourcehansans_content'
codeStyle.fontSize = 8
codeStyle.spaceBefore = 4
codeStyle.spaceAfter = 4
codeStyle.leading = 12
codeStyle.bulletFontSize = 14
codeStyle.bulletOffsetY = -2
codeStyle.bulletIndent = 14

global bulletStyle
bulletStyle = copy.deepcopy(styleBL)
bulletStyle.fontName = 'sourcehansans_content'
bulletStyle.fontSize = 8
bulletStyle.spaceBefore = 4
bulletStyle.spaceAfter = 4
bulletStyle.firstLineIndent = 16
bulletStyle.leading = 12
bulletStyle.bulletFontSize = 14
bulletStyle.bulletOffsetY = -2
bulletStyle.bulletIndent = 4

global secondaryItemStyle
secondaryItemStyle = copy.deepcopy(styleBL)
secondaryItemStyle.fontName = 'sourcehansans_content'
secondaryItemStyle.fontSize = 8
secondaryItemStyle.spaceBefore = 4
secondaryItemStyle.spaceAfter = 4
secondaryItemStyle.firstLineIndent = 16
secondaryItemStyle.leading = 12
secondaryItemStyle.bulletFontSize = 14
secondaryItemStyle.bulletOffsetY = -2
secondaryItemStyle.bulletIndent = 20

class Pdfcreator(object):
   
    # def __init__(self, vendorDir):
    def __init__(self):
        img_dir = os.path.join(os.path.split(os.path.realpath(__file__))[0], "..", "etc", "report-img")
        self.__watermarkImgPath = os.path.join(img_dir, 'report_watermark.png')
        self.__wmw, self.__wmh = utils.ImageReader(self.__watermarkImgPath).getSize()

    def __AllPageSetup(self, canvas, doc):
        canvas.saveState()

        #header
        canvas.setFont("sourcehansans_title", 6)
        canvas.setFillColor(colors.royalblue)

        #watermark
        pw = PAGE_SIZE[0]
        ph = PAGE_SIZE[1]
        wc = int(pw/self.__wmw) + 1
        hc = int(ph/self.__wmh) + 1
        for i in range(0, wc):
            for j in range(0, hc):
                canvas.drawImage(self.__watermarkImgPath, i * self.__wmw,  j * self.__wmh, mask='auto')

        #footers
        canvas.setFont("sourcehansans_content", 6)
        canvas.setFillColor(colors.royalblue)
        canvas.drawString(PAGE_SIZE[0] / 2 - 0.1 * inch, 0.1 * inch, '- %d -' % (doc.page))
        
        canvas.restoreState()

    def __build_story(self, pdfPath, story):
        baseDocTemplate = BaseDocTemplate(pdfPath, pagesize=PAGE_SIZE,
                                    leftMargin=MARGIN_SIZE, rightMargin=MARGIN_SIZE,
                                    topMargin=MARGIN_SIZE, bottomMargin=MARGIN_SIZE,
                                    allowSplitting=0)
        main_frame = Frame(MARGIN_SIZE, MARGIN_SIZE,
                            PAGE_SIZE[0] - 2 * MARGIN_SIZE, PAGE_SIZE[1] - 2 * MARGIN_SIZE,
                            leftPadding = 0, rightPadding = 0, bottomPadding = 0,
                            topPadding = 0, id = 'main_frame',showBoundary=0)
        main_template = PageTemplate(id = 'Page', frames = [main_frame],autoNextPageTemplate=0,onPage=self.__AllPageSetup)
        baseDocTemplate.addPageTemplates(main_template)
        baseDocTemplate.build(story)

    def __genImage(self, path, width=1*cm):
        img = utils.ImageReader(path)
        iw, ih = img.getSize()
        aspect = ih / float(iw)
        return Image(path, width=width, height=(width * aspect))

    def __genLink(self, string, url):
        return '<link href="' + url + '">' + string + '</link>'

    def __registerFont(self):
        # reportlab.rl_config.warnOnMissingFontGlyphs = 0
        # pdfmetrics.registerFont(TTFont('sourcehansans', self.__font_path))
        # fonts.addMapping('sourcehansans', 0, 0, 'sourcehansans')
        # # pdfmetrics.registerFontFamily('sourcehansans',normal='sourcehansans',bold='sourcehansans-bold')

        font_dir = os.path.join(os.path.split(
            os.path.realpath(__file__))[0], "..", "font")
        sourcehansans_font_path_titele = os.path.join(font_dir, 'SourceHanSansCN-Regular.ttf')
        sourcehansans_font_path_content = os.path.join(font_dir, 'SourceHanSansCN-Light.ttf')
        reportlab.rl_config.warnOnMissingFontGlyphs = 0
        pdfmetrics.registerFont(TTFont('sourcehansans_title', sourcehansans_font_path_titele))
        pdfmetrics.registerFont(TTFont('sourcehansans_content', sourcehansans_font_path_content))
        fonts.addMapping('sourcehansans_title', 0, 0, 'sourcehansans_title')
        fonts.addMapping('sourcehansans_content', 0, 0, 'sourcehansans_content')

    def __isLink(self, body):
        text = body
        text.strip()
        sep1 = text.find('[')
        sep2 = text.find('](')
        sep3 = text.find(')')

        link = text[sep2+2:sep3]
        if link.find('.') < 0:
            return False

        if sep1 >= 0 and sep2 > sep1 and sep3 == len(text) - 1:
            return True
        return False


    def __appendBody(self, story, body, style, pretext):
        newbody = ''
        cur = 0
        sep1 = -1
        sep2 = -1
        sep3 = -1

        while cur < len(body):
            sep2 = body.find('](', cur)
            if sep2 > cur:
                sep1 = body.rfind('[', cur, sep2)
                sep3 = body.find(')', sep2)
                if sep1 >= cur and sep3 > sep2:
                    if sep1 > cur and body[sep1 - 1] == '!':
                        story.append(Paragraph(pretext + escape(newbody), style))
                        story.append(self.__genImage(body[sep2+2:sep3], width=17*cm))
                        cur = sep3 + 1
                        continue
                    elif sep1 > cur:
                        newbody += escape(body[cur:sep1])

                    if self.__isLink(body[sep1:sep3+1]):
                        newbody = newbody + '<font color = mediumblue>' + self.__genLink(body[sep1+1:sep2], escape(body[sep2+2:sep3])) + '</font>'
                    else:
                        newbody = newbody + body[sep1:sep3+1]
                    cur = sep3 + 1
                    continue
            
            newbody += escape(body[cur:])
            break
        if self.__isLink(body):
            style = linkStyle
        story.append(Paragraph(pretext + newbody, style))

    def createReport(self, mdPath, pdfPath):
        self.__registerFont()

        # create doc object
        story = [Spacer(width=4 * cm, height=7 * cm)]

        lines = []
            
        content = ''
        with codecs.open(os.path.realpath(mdPath), "r", 'utf-8') as f:
            content = f.read().replace('`', '').replace('</center>', '')
            lines = content.splitlines()
            # print len(lines)
            f.close()

        lineNumber = 1;
        myStyle = copy.deepcopy(bulletStyle)
        myStyle.alignment = 1
        for line in lines:
            # x.strip()
            x = str(line)
            
            if x.startswith('# <center>'):
                # report title
                self.__appendBody(story, x[10:], titleStyle, '')
            elif x.startswith('### <center>') and lineNumber == 3:
                # vendor and date
                self.__appendBody(story, x[12:], slaveTitleStyle, '')
                story.append(PageBreak())
            elif x.startswith('## '):
                story.append(Paragraph(x[3:], subtitleStyle))
            elif x.startswith('### '):
                story.append(Paragraph(x[4:], lv3titleStyle))
            elif x.startswith('#### '):
                story.append(Paragraph(x[5:], lv4titleStyle))
            elif line.startswith('* '):
                self.__appendBody(story, line[2:], bulletStyle, '<bullet>&bull;</bullet>')
            elif line.startswith('    - '):
                self.__appendBody(story, line[6:], bulletStyle, '<bullet>&nbsp;&nbsp;&nbsp;&nbsp;&deg;</bullet>')
            elif x.startswith('<center>'):
                self.__appendBody(story, x[8:], myStyle, '')
            else:
                self.__appendBody(story, x, bulletStyle, '')
            lineNumber += 1

        self.__build_story(os.path.realpath(pdfPath), story)
        return 0

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Pdfcreator", )
    parser.add_argument("-o", "--output", type=str, dest="output",
                        default="", help="output pdf file", required=True)
    parser.add_argument("-i", "--input", type=str, dest="input",
                        default="", help="input markdown file", required=True)
    parser.add_argument("-v", "--vendor", type=str, dest="vendor",
                        default="", help="vendor dir path", required=True)

    args = [arg for arg in sys.argv if arg != "__ignore_me__"]
    args = parser.parse_args(args[1:])

    pdfcreator = Pdfcreator(args.vendor)
    pdfcreator.createReport(args.input, args.output)
