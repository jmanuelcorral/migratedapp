import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  tasks: Task[] = [];
  totalTasks = 0;
  pendingTasks = 0;
  inProgressTasks = 0;
  completedTasks = 0;

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.taskService.getAllTasks().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.calculateStats();
      },
      error: (error) => {
        console.error('Error loading tasks:', error);
      }
    });
  }

  calculateStats(): void {
    this.totalTasks = this.tasks.length;
    this.pendingTasks = this.tasks.filter(t => t.status === 0).length;
    this.inProgressTasks = this.tasks.filter(t => t.status === 1).length;
    this.completedTasks = this.tasks.filter(t => t.status === 2).length;
  }
}
