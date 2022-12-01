<?php
$qstring = $_SERVER['QUERY_STRING'];

if (empty($qstring)) {
    $og_img_link = "{{settings.main-site}}{{settings.default-preview-image}}";
} else {
    parse_str ( parse_url ( '?'.$qstring , PHP_URL_QUERY ) , $params );
    $savestate = $params["s"];
    $savestate_img_filename = "previews/".$savestate.".png";
    $savestate_img_filename_absolute = dirname(__FILE__) . "/" . $savestate_img_filename;

    $dynamic_img_link = "{{settings.preview-api}}?".$qstring;

    if (! file_exists($savestate_img_filename_absolute)) {
        file_put_contents($savestate_img_filename, file_get_contents($dynamic_img_link));
        $og_img_link = $dynamic_img_link;
    }
    else {
        $og_img_link = $savestate_img_filename;
    }

}
?>
