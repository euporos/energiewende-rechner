<?php
$qstring = $_SERVER['QUERY_STRING'];

if (empty($qstring)) {
    $og_img_link = "{{settings.main-site}}{{settings.default-preview-image}}";
} else {
    $og_img_link = "{{settings.preview-api}}?".$qstring;
}
echo $og_img_link;
?>
