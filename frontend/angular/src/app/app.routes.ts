import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { TaskListComponent } from './components/task-list/task-list.component';
import { TaskEditComponent } from './components/task-edit/task-edit.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'tasks', component: TaskListComponent },
  { path: 'tasks/new', component: TaskEditComponent },
  { path: 'tasks/:id/edit', component: TaskEditComponent },
  { path: '**', redirectTo: '' }
];
