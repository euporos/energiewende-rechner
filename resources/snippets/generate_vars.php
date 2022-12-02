<?php

$preview_dir = "previews";

if (! file_exists($preview_dir)) {
    mkdir(
    $preview_dir
    );
}

$bare_query_string = $_SERVER['QUERY_STRING'];
$full_query_string = "?".$bare_query_string;
$full_url = "https://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]";

if (empty($bare_query_string)) {
    $og_img_link = "{{settings.main-site}}{{settings.default-preview-image}}";
} else {
    parse_str ( parse_url ( '?'.$bare_query_string , PHP_URL_QUERY ) , $params );
    $savestate = $params["s"];
    $savestate_img_filename = "previews/".$savestate.".png";
    $savestate_img_filename_absolute = dirname(__FILE__) . "/" . $savestate_img_filename;

    $dynamic_img_link = "{{settings.preview-api}}?".$bare_query_string;

    if (! file_exists($savestate_img_filename_absolute)) {
        file_put_contents($savestate_img_filename, file_get_contents($dynamic_img_link));
    }

        $static_image_link = "https://$_SERVER[HTTP_HOST]/$savestate_img_filename";
        $og_img_link = $static_image_link;

}
?>
