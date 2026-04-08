<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'quiz_result')]
class QuizResult
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function setId(int $id): self
    {
        $this->id = $id;
        return $this;
    }

    #[ORM\Column(type: 'string', nullable: false)]
    private ?string $student_name = null;

    public function getStudent_name(): ?string
    {
        return $this->student_name;
    }

    public function setStudent_name(string $student_name): self
    {
        $this->student_name = $student_name;
        return $this;
    }

    #[ORM\Column(type: 'integer', nullable: false)]
    private ?int $lesson_id = null;

    public function getLesson_id(): ?int
    {
        return $this->lesson_id;
    }

    public function setLesson_id(int $lesson_id): self
    {
        $this->lesson_id = $lesson_id;
        return $this;
    }

    #[ORM\Column(type: 'string', nullable: false)]
    private ?string $lesson_title = null;

    public function getLesson_title(): ?string
    {
        return $this->lesson_title;
    }

    public function setLesson_title(string $lesson_title): self
    {
        $this->lesson_title = $lesson_title;
        return $this;
    }

    #[ORM\Column(type: 'string', nullable: false)]
    private ?string $formation_title = null;

    public function getFormation_title(): ?string
    {
        return $this->formation_title;
    }

    public function setFormation_title(string $formation_title): self
    {
        $this->formation_title = $formation_title;
        return $this;
    }

    #[ORM\Column(type: 'integer', nullable: false)]
    private ?int $score = null;

    public function getScore(): ?int
    {
        return $this->score;
    }

    public function setScore(int $score): self
    {
        $this->score = $score;
        return $this;
    }

    #[ORM\Column(type: 'integer', nullable: false)]
    private ?int $passed = null;

    public function getPassed(): ?int
    {
        return $this->passed;
    }

    public function setPassed(int $passed): self
    {
        $this->passed = $passed;
        return $this;
    }

    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $taken_at = null;

    public function getTaken_at(): ?\DateTimeInterface
    {
        return $this->taken_at;
    }

    public function setTaken_at(\DateTimeInterface $taken_at): self
    {
        $this->taken_at = $taken_at;
        return $this;
    }

    public function getStudentName(): ?string
    {
        return $this->student_name;
    }

    public function setStudentName(string $student_name): static
    {
        $this->student_name = $student_name;

        return $this;
    }

    public function getLessonId(): ?int
    {
        return $this->lesson_id;
    }

    public function setLessonId(int $lesson_id): static
    {
        $this->lesson_id = $lesson_id;

        return $this;
    }

    public function getLessonTitle(): ?string
    {
        return $this->lesson_title;
    }

    public function setLessonTitle(string $lesson_title): static
    {
        $this->lesson_title = $lesson_title;

        return $this;
    }

    public function getFormationTitle(): ?string
    {
        return $this->formation_title;
    }

    public function setFormationTitle(string $formation_title): static
    {
        $this->formation_title = $formation_title;

        return $this;
    }

    public function getTakenAt(): ?\DateTimeInterface
    {
        return $this->taken_at;
    }

    public function setTakenAt(?\DateTimeInterface $taken_at): static
    {
        $this->taken_at = $taken_at;

        return $this;
    }
}