<?php

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

    $og_img_link = $dynamic_img_link;

}
?>
