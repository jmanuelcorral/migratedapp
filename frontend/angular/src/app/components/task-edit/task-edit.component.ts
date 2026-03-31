import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TaskService } from '../../services/task.service';
import { TaskRequest } from '../../models/task.model';

@Component({
  selector: 'app-task-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './task-edit.component.html'
})
export class TaskEditComponent implements OnInit {
  taskForm: FormGroup;
  isEditMode = false;
  taskId: number | null = null;
  submitted = false;

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.taskForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      status: [0, Validators.required],
      dueDate: ['']
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.taskId = parseInt(id);
      this.loadTask(this.taskId);
    }
  }

  loadTask(id: number): void {
    this.taskService.getTaskById(id).subscribe({
      next: (task) => {
        this.taskForm.patchValue({
          title: task.title,
          description: task.description || '',
          status: task.status,
          dueDate: task.dueDate ? task.dueDate.split('T')[0] : ''
        });
      },
      error: (error) => {
        console.error('Error loading task:', error);
        alert('Failed to load task. Please try again.');
        this.router.navigate(['/tasks']);
      }
    });
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.taskForm.invalid) {
      return;
    }

    const formValue = this.taskForm.value;
    const request: TaskRequest = {
      title: formValue.title,
      description: formValue.description || null,
      status: parseInt(formValue.status),
      dueDate: formValue.dueDate || null
    };

    if (this.isEditMode && this.taskId !== null) {
      this.taskService.updateTask(this.taskId, request).subscribe({
        next: () => {
          this.router.navigate(['/tasks']);
        },
        error: (error) => {
          console.error('Error updating task:', error);
          alert('Failed to update task. Please try again.');
        }
      });
    } else {
      this.taskService.createTask(request).subscribe({
        next: () => {
          this.router.navigate(['/tasks']);
        },
        error: (error) => {
          console.error('Error creating task:', error);
          alert('Failed to create task. Please try again.');
        }
      });
    }
  }

  get f() {
    return this.taskForm.controls;
  }
}
