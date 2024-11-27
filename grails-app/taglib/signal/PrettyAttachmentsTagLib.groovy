package signal

class PrettyAttachmentsTagLib {
    static defaultEncodeAs = [taglib:'html']

    static namespace = 'prettyAttachments'

    static final Map FILE_ICON_MAP = [
            pdf: 'fa-file-pdf-o',
            doc: 'fa-file-word-o',
            docx: 'fa-file-word-o',
            xls: 'fa-file-excel-o',
            xlsx: 'fa-file-excel-o',
            ppt: 'a-file-powerpoint-o',
            gif: 'fa-file-image-o',
            png: 'fa-file-image-o',
            jpg: 'fa-file-image-o',
            jpeg: 'fa-file-image-o',
            bmp: 'fa-file-image-o',
            tif: 'fa-file-image-o',
            mov: 'fa-file-video-o',
            wav: 'fa-file-audio-o',
            mp3: 'fa-file-audio-o',
            raw: 'fa-file-image-o',
            swf: 'fa-file-image-o',
            txt: 'fa-file-text-o',
            zip: 'fa-file-archive-o',
            xml: 'fa-file-code-o',
            htm: 'fa-file-code-o',
            html: 'fa-file-code-o',
            xsl: 'fa-file-code-o',
            groovy: 'fa-file-code-o',
            php: 'fa-file-code-o'
    ]

    def icon = {attrs ->
        def bean = attrs.attachment
        if(! bean) {
            throwTagError("Tag [icon] is missing required attribute [attachment]")
        }

        out << "fa " + (FILE_ICON_MAP[bean.ext?.toLowerCase()] ?: 'fa-file-o') + " fa-2x"
    }
}
