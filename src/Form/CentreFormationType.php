<?php

namespace App\Form;

use App\Entity\CentreFormation;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\CheckboxType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\HiddenType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\UrlType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class CentreFormationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $villes = [
            'Tunis' => 'Tunis', 'Ariana' => 'Ariana', 'Ben Brous' => 'Ben Arous',
            'Manouba' => 'Manouba', 'Nabeul' => 'Nabeul', 'Zaghouan' => 'Zaghouan',
            'Bizerte' => 'Bizerte', 'Béja' => 'Béja', 'Jendouba' => 'Jendouba',
            'Kef' => 'Kef', 'Siliana' => 'Siliana', 'Sousse' => 'Sousse',
            'Monastir' => 'Monastir', 'Mahdia' => 'Mahdia', 'Sfax' => 'Sfax',
            'Kairouan' => 'Kairouan', 'Kasserine' => 'Kasserine', 'Sidi Bouzid' => 'Sidi Bouzid',
            'Gabès' => 'Gabès', 'Médenine' => 'Médenine', 'Tataouine' => 'Tataouine',
            'Gafsa' => 'Gafsa', 'Tozeur' => 'Tozeur', 'Kébili' => 'Kébili',
        ];

        $builder
            ->add('nom', TextType::class, [
                'label' => 'Nom du centre',
                'attr'  => ['placeholder' => 'Ex: Centre Finora Tunis']
            ])
            ->add('adresse', TextType::class, [
                'label' => 'Adresse complète',
                'attr'  => ['placeholder' => 'Ex: 12 Rue de la République']
            ])
            ->add('ville', ChoiceType::class, [
                'label'       => 'Gouvernorat / Ville',
                'placeholder' => '— Choisir —',
                'choices'     => $villes,
            ])
            ->add('description', TextareaType::class, [
                'label'    => 'Description',
                'required' => false,
                'attr'     => ['rows' => 3, 'placeholder' => 'Horaires, spécialités, informations pratiques…']
            ])
            ->add('telephone', TextType::class, [
                'label'    => 'Téléphone',
                'required' => false,
                'attr'     => ['placeholder' => '+216 xx xxx xxx']
            ])
            ->add('email', EmailType::class, [
                'label'    => 'Email',
                'required' => false,
            ])
            ->add('siteWeb', UrlType::class, [
                'label'               => 'Site web',
                'required'            => false,
                'default_protocol'    => 'https',
                'attr'                => ['placeholder' => 'https://…']
            ])
            ->add('latitude', HiddenType::class)
            ->add('longitude', HiddenType::class)
            ->add('isActive', CheckboxType::class, [
                'label'    => 'Visible sur la carte publique',
                'required' => false,
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => CentreFormation::class]);
    }
}
