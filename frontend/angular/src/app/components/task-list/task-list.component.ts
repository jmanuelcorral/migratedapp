import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './task-list.component.html'
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  filteredTasks: Task[] = [];
  filterStatus = 'all';

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.taskService.getAllTasks().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.applyFilter();
      },
      error: (error) => {
        console.error('Error loading tasks:', error);
        alert('Failed to load tasks. Please try again.');
      }
    });
  }

  applyFilter(): void {
    if (this.filterStatus === 'all') {
      this.filteredTasks = this.tasks;
    } else {
      const status = parseInt(this.filterStatus);
      this.filteredTasks = this.tasks.filter(t => t.status === status);
    }
  }

  onFilterChange(): void {
    this.applyFilter();
  }

  deleteTask(task: Task): void {
    if (confirm(`Are you sure you want to delete task "${task.title}"?`)) {
      this.taskService.deleteTask(task.id).subscribe({
        next: () => {
          this.loadTasks();
        },
        error: (error) => {
          console.error('Error deleting task:', error);
          alert('Failed to delete task. Please try again.');
        }
      });
    }
  }

  getStatusBadgeClass(status: number): string {
    switch (status) {
      case 0: return 'badge bg-warning text-dark';
      case 1: return 'badge bg-info';
      case 2: return 'badge bg-success';
      default: return 'badge bg-secondary';
    }
  }

  formatDate(dateStr: string | null): string {
    if (!dateStr) return '—';
    const date = new Date(dateStr);
    return date.toISOString().split('T')[0];
  }
}
