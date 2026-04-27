<?php
echo "Current File: " . __FILE__ . "<br>";
echo "Current Dir: " . __DIR__ . "<br>";
echo "Document Root: " . $_SERVER['DOCUMENT_ROOT'] . "<br>";
echo "Included files: <pre>";
print_r(get_included_files());
echo "</pre>";
