package com.rxlogix.enums

import com.rxlogix.util.DateUtil

public enum TimeZoneEnum {
    TZ_0(0, "Etc/GMT+12"),
    TZ_1(1, "Pacific/Pago_Pago"),
    TZ_2(2, "Pacific/Honolulu"),
    TZ_3(3, "America/Anchorage"),
    TZ_4(4, "America/Los_Angeles"),
    TZ_10(10, "America/Edmonton"),
    TZ_13(13, "America/Mazatlan"),
    TZ_15(15, "America/Phoenix"),
    TZ_20(20, "America/Winnipeg"),
    TZ_25(25, "America/Regina"),
    TZ_30(30, "America/Mexico_City"),
    TZ_33(33, "America/Chicago"),
    TZ_35(35, "America/New_York"),
    TZ_40(40, "America/Indiana/Indianapolis"),
    TZ_45(45, "America/Lima"),
    TZ_50(50, "America/Halifax"),
    TZ_55(55, "America/Caracas"),
    TZ_56(56, "America/Santiago"),
    TZ_60(60, "America/St_Johns"),
    TZ_65(65, "America/Sao_Paulo"),
    TZ_70(70, "America/Argentina/Buenos_Aires"),
    TZ_73(73, "America/Godthab"),
    TZ_75(75, "Etc/GMT+2"),
    TZ_80(80, "Atlantic/Azores"),
    TZ_83(83, "Atlantic/Cape_Verde"),
    TZ_85(85, "Europe/London"),
    TZ_90(90, "Africa/Casablanca"),
    TZ_95(95, "Europe/Belgrade"),
    TZ_100(100, "Europe/Warsaw"),
    TZ_105(105, "Europe/Paris"),
    TZ_110(110, "Europe/Berlin"),
    TZ_113(113, "Africa/Malabo"),
    TZ_115(115, "Europe/Bucharest"),
    TZ_120(120, "Africa/Cairo"),
    TZ_125(125, "Europe/Sofia"),
    TZ_130(130, "Europe/Athens"),
    TZ_135(135, "Asia/Jerusalem"),
    TZ_140(140, "Africa/Harare"),
    TZ_145(145, "Europe/Moscow"),
    TZ_150(150, "Asia/Kuwait"),
    TZ_155(155, "Africa/Nairobi"),
    TZ_158(158, "Asia/Baghdad"),
    TZ_160(160, "Asia/Tehran"),
    TZ_165(165, "Asia/Dubai"),
    TZ_170(170, "Asia/Baku"),
    TZ_175(175, "Asia/Kabul"),
    TZ_180(180, "Asia/Yekaterinburg"),
    TZ_185(185, "Asia/Karachi"),
    TZ_190(190, "Asia/Kolkata"),
    TZ_193(193, "Asia/Kathmandu"),
    TZ_195(195, "Asia/Dhaka"),
    TZ_200(200, "Asia/Colombo"),
    TZ_201(201, "Asia/Almaty"),
    TZ_203(203, "Asia/Rangoon"),
    TZ_205(205, "Asia/Bangkok"),
    TZ_207(207, "Asia/Krasnoyarsk"),
    TZ_210(210, "Asia/Hong_Kong"),
    TZ_215(215, "Asia/Singapore"),
    TZ_220(220, "Asia/Taipei"),
    TZ_225(225, "Australia/Perth"),
    TZ_227(227, "Asia/Irkutsk"),
    TZ_230(230, "Asia/Seoul"),
    TZ_235(235, "Asia/Tokyo"),
    TZ_240(240, "Asia/Yakutsk"),
    TZ_245(245, "Australia/Darwin"),
    TZ_250(250, "Australia/Adelaide"),
    TZ_255(255, "Australia/Sydney"),
    TZ_260(260, "Australia/Brisbane"),
    TZ_265(265, "Australia/Hobart"),
    TZ_270(270, "Asia/Vladivostok"),
    TZ_275(275, "Pacific/Guam"),
    TZ_280(280, "Asia/Magadan"),
    TZ_285(285, "Pacific/Fiji"),
    TZ_290(290, "Pacific/Auckland"),
    TZ_300(300, "Pacific/Enderbury"),
    TZ_301(301, "Etc/GMT+11"),
    TZ_302(302, "Etc/GMT+10"),
    TZ_303(303, "Etc/GMT+9"),
    TZ_304(304, "Etc/GMT+8"),
    TZ_305(305, "Etc/GMT+7"),
    TZ_306(306, "Etc/GMT+6"),
    TZ_307(307, "Etc/GMT+5"),
    TZ_308(308, "Etc/GMT+4"),
    TZ_309(309, "Etc/GMT+3"),
    TZ_310(310, "Etc/GMT+1"),
    TZ_311(311, "Etc/GMT"),
    TZ_312(312, "Etc/GMT-1"),
    TZ_313(313, "Etc/GMT-2"),
    TZ_314(314, "Etc/GMT-3"),
    TZ_315(315, "Etc/GMT-4"),
    TZ_316(316, "Etc/GMT-5"),
    TZ_317(317, "Etc/GMT-6"),
    TZ_318(318, "Etc/GMT-7"),
    TZ_319(319, "Etc/GMT-8"),
    TZ_320(320, "Etc/GMT-9"),
    TZ_321(321, "Etc/GMT-10"),
    TZ_322(322, "Etc/GMT-11"),
    TZ_323(323, "Etc/GMT-12"),
    TZ_324(324, "Etc/GMT-13"),
    TZ_325(325, "PST8PDT"),
    TZ_326(326, "MST7MDT"),
    TZ_327(327, "CST6CDT"),
    TZ_328(328, "EST5EDT"),
    TZ_329(329, "EST"),
    TZ_330(330, "UTC")

    private long index
    private String timezoneId

    TimeZoneEnum(long index, String timezoneId) {
        this.index = index
        this.timezoneId = timezoneId
    }

    def getGmtOffset() {
        return DateUtil.getOffsetString(this.timezoneId)
    }

    public getIndex() {
        return index
    }

    public getTimezoneId() {
        return timezoneId
    }

    public getI18nKey() {
        return "app.timezone.${this.name()}"
    }
}
