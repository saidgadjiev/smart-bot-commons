package ru.gadjini.telegram.smart.bot.commons.service.format;

public enum Format {

    PPTX(FormatCategory.DOCUMENTS),
    PPT(FormatCategory.DOCUMENTS),
    PPTM(FormatCategory.DOCUMENTS),
    POTX(FormatCategory.DOCUMENTS),
    POT(FormatCategory.DOCUMENTS),
    POTM(FormatCategory.DOCUMENTS),
    PPS(FormatCategory.DOCUMENTS),
    PPSX(FormatCategory.DOCUMENTS),
    PPSM(FormatCategory.DOCUMENTS),
    XLSX(FormatCategory.DOCUMENTS),
    XLS(FormatCategory.DOCUMENTS),
    DOC(FormatCategory.DOCUMENTS),
    DOCX(FormatCategory.DOCUMENTS),
    RTF(FormatCategory.DOCUMENTS),
    PDF(FormatCategory.DOCUMENTS),
    PNG(FormatCategory.IMAGES),
    HEIC(FormatCategory.IMAGES),
    HEIF(FormatCategory.IMAGES),
    ICO(FormatCategory.IMAGES),
    SVG(FormatCategory.IMAGES),
    JPG(FormatCategory.IMAGES),
    JP2(FormatCategory.IMAGES),
    BMP(FormatCategory.IMAGES),
    TXT(FormatCategory.DOCUMENTS),
    TIFF(FormatCategory.IMAGES),
    EPUB(FormatCategory.DOCUMENTS),
    WEBP(FormatCategory.IMAGES),
    PHOTO(FormatCategory.IMAGES),
    TGS(FormatCategory.IMAGES),
    GIF(FormatCategory.IMAGES),
    STICKER(FormatCategory.IMAGES) {
        @Override
        public String getExt() {
            return "webp";
        }
    },
    HTML(FormatCategory.WEB),
    HTMLZ(FormatCategory.WEB),
    URL(FormatCategory.WEB),
    TEXT(FormatCategory.DOCUMENTS),
    ZIP(FormatCategory.ARCHIVE),
    RAR(FormatCategory.ARCHIVE),
    AZW(FormatCategory.DOCUMENTS),
    AZW3(FormatCategory.DOCUMENTS),
    AZW4(FormatCategory.DOCUMENTS),
    CBZ(FormatCategory.DOCUMENTS),
    CBR(FormatCategory.DOCUMENTS),
    CBC(FormatCategory.DOCUMENTS),
    CHM(FormatCategory.DOCUMENTS),
    DJVU(FormatCategory.DOCUMENTS),
    FB2(FormatCategory.DOCUMENTS),
    FBZ(FormatCategory.DOCUMENTS),
    LIT(FormatCategory.DOCUMENTS),
    LRF(FormatCategory.DOCUMENTS),
    MOBI(FormatCategory.DOCUMENTS),
    ODT(FormatCategory.DOCUMENTS),
    PRC(FormatCategory.DOCUMENTS),
    PDB(FormatCategory.DOCUMENTS),
    PML(FormatCategory.DOCUMENTS),
    RB(FormatCategory.DOCUMENTS),
    SNB(FormatCategory.DOCUMENTS),
    TCR(FormatCategory.DOCUMENTS),
    TXTZ(FormatCategory.DOCUMENTS),
    OEB(FormatCategory.DOCUMENTS),
    PMLZ(FormatCategory.DOCUMENTS),
    MP4(FormatCategory.VIDEO),
    _3GP(FormatCategory.VIDEO) {
        @Override
        public String getExt() {
            return "3gp";
        }

        @Override
        public String getName() {
            return "3GP";
        }
    },
    AVI(FormatCategory.VIDEO),
    FLV(FormatCategory.VIDEO),
    M4V(FormatCategory.VIDEO),
    MKV(FormatCategory.VIDEO),
    MOV(FormatCategory.VIDEO),
    MPEG(FormatCategory.VIDEO),
    MPG(FormatCategory.VIDEO),
    MTS(FormatCategory.VIDEO),
    VOB(FormatCategory.VIDEO),
    WEBM(FormatCategory.VIDEO),
    WMV(FormatCategory.VIDEO),
    IMAGES(FormatCategory.IMAGES),
    COMPRESS(FormatCategory.VIDEO) {
        @Override
        public String getExt() {
            return "mp4";
        }
    },
    AAC(FormatCategory.AUDIO),
    AMR(FormatCategory.AUDIO),
    AIFF(FormatCategory.AUDIO),
    FLAC(FormatCategory.AUDIO),
    MP3(FormatCategory.AUDIO),
    OGG(FormatCategory.AUDIO),
    WAV(FormatCategory.AUDIO),
    WMA(FormatCategory.AUDIO);

    private FormatCategory category;

    Format(FormatCategory category) {
        this.category = category;
    }

    public String getExt() {
        return name().toLowerCase();
    }

    public String getName() {
        return name();
    }

    public FormatCategory getCategory() {
        return category;
    }
}
