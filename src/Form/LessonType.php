<?php

namespace App\Form;

use App\Entity\Formation;
use App\Entity\Lesson;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class LessonType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('formation', EntityType::class, [
                'class' => Formation::class,
                'choice_label' => 'titre',
                'label' => 'Formation',
                'placeholder' => 'Choisir une formation',
            ])
            ->add('titre', TextType::class, [
                'label' => 'Titre',
                'attr' => [
                    'placeholder' => 'Entrez le titre de la lesson'
                ]
            ])
            ->add('contenu', TextareaType::class, [
                'label' => 'Contenu',
                'required' => false,
                'attr' => [
                    'placeholder' => 'Entrez le contenu'
                ]
            ])
            ->add('videoUrl', TextType::class, [
                'label' => 'URL vidéo',
                'required' => false,
                'attr' => [
                    'placeholder' => 'https://youtube.com/... ou https://...mp4'
                ]
            ])
            ->add('ordre', IntegerType::class, [
                'label' => 'Ordre',
            ])
            ->add('dureeMinutes', IntegerType::class, [
                'label' => 'Durée (minutes)',
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Lesson::class,
        ]);
    }
}