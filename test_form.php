<?php
require 'vendor/autoload.php';
use Symfony\Component\Form\Forms;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;

$factory = Forms::createFormFactory();
$form = $factory->createBuilder(ChoiceType::class, null, [
    'choices' => ['Income' => 'INCOME', 'Outcome' => 'OUTCOME'],
    'expanded' => true,
    'multiple' => false,
])->getForm();

$view = $form->createView();
foreach ($view->children as $child) {
    echo "ID: " . $child->vars['id'] . "\n";
    echo "Name: " . $child->vars['full_name'] . "\n";
    echo "Value: " . $child->vars['value'] . "\n";
    echo "---\n";
}
