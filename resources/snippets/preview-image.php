<?php
$qstring = $_SERVER['QUERY_STRING'];

if (empty($qstring)) {
    $og_img_link = "{{settings.main-site}}{{settings.default-preview-image}}";
} else {
    parse_str ( parse_url ( '?'.$qstring , PHP_URL_QUERY ) , $params );
    $savestate = $params["s"];
    $savestate_img_filename = "previews/".$savestate.".png";
    $savestate_img_filename_absolute = dirname(__FILE__) . "/" . $savestate_img_filename;

    $og_img_link = "{{settings.preview-api}}?".$qstring;

    if (! file_exists($savestate_img_filename_absolute)) {
        file_put_contents($savestate_img_filename, file_get_contents($og_img_link));
        echo $og_img_link;
    }
    else {
        echo $savestate_img_filename;
    }

    echo $savestate_img_filename_absolute;

}
?>
