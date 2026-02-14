import { Routes } from '@angular/router';
import { AuthGuard } from '../../utils/AuthGuard';

export const routes: Routes = [
  {
    path: '',
    data: {
      title: 'Base'
    },
    children: [
      {
        path: '',
        redirectTo: 'cards',
        pathMatch: 'full'
      },
      {
        path: 'tasks',
        loadComponent: () => import('./tasks/tasks.component').then(m => m.TasksComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'Tasks'
        }
      },
      {
        path: 'tasks/initiate-task',
        loadComponent: () => import('./tasks/tasks-stepper/tasks-stepper.component').then(m => m.TasksStepperComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'Initiate Task'
        }
      },
      {
        path: 'tasks/:id/view',
        loadComponent: () => import('./tasks/view-task/view-task.component').then(m => m.ViewTaskComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'View Task'
        }
      },
      {
        path: 'tasks/:id/edit',
        loadComponent: () => import('./tasks/edit-task/edit-task.component').then(m => m.EditTaskComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'Edit Task'
        }
      },
      {
        path: 'tasks/:id/update-progress',
        loadComponent: () => import('./tasks/update-task-progress/update-task-progress.component').then(m => m.UpdateTaskProgressComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'Update Task Progress'
        }
      },
      {
        path: 'donors',
        loadComponent: () => import('./donor/donor.component').then(m => m.DonorComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'Donors'
        }
      },
      {
        path: 'donors/create',
        loadComponent: () => import('./donor/create-donor/create-donor.component').then(m => m.CreateDonorComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'Create Donor'
        }
      },
      {
        path: 'donors/:id/view',
        loadComponent: () => import('./donor/view-donor/view-donor.component').then(m => m.ViewDonorComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'View Donor'
        }
      },
      {
        path: 'donors/:id/edit',
        loadComponent: () => import('./donor/edit-donor/edit-donor.component').then(m => m.EditDonorComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'Edit Donor'
        }
      },
      {
        path: 'partners',
        loadComponent: () => import('./partner/partners.component').then(m => m.PartnersComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'Partners'
        }
      },
      {
        path: 'partners/create',
        loadComponent: () => import('./partner/create-partner/create-partner.component').then(m => m.CreatePartnerComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'Create Partner'
        }
      },
      {
        path: 'partners/:id/view',
        loadComponent: () => import('./partner/view-partner/view-partner.component').then(m => m.ViewPartnerComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'View Partner'
        }
      },
      {
        path: 'partners/:id/edit',
        loadComponent: () => import('./partner/edit-partner/edit-partner.component').then(m => m.EditPartnerComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'Edit Partner'
        }
      },
      {
        path: 'accordion',
        loadComponent: () => import('./accordion/accordions.component').then(m => m.AccordionsComponent),
        data: {
          title: 'Accordion'
        }
      },
      {
        path: 'breadcrumbs',
        loadComponent: () => import('./breadcrumbs/breadcrumbs.component').then(m => m.BreadcrumbsComponent),
        data: {
          title: 'Breadcrumbs'
        }
      },
      {
        path: 'cards',
        loadComponent: () => import('./cards/cards.component').then(m => m.CardsComponent),
        data: {
          title: 'Cards'
        }
      },
      {
        path: 'carousel',
        loadComponent: () => import('./carousels/carousels.component').then(m => m.CarouselsComponent),
        data: {
          title: 'Carousel'
        }
      },
      {
        path: 'collapse',
        loadComponent: () => import('./collapses/collapses.component').then(m => m.CollapsesComponent),
        data: {
          title: 'Collapse'
        }
      },
      {
        path: 'list-group',
        loadComponent: () => import('./list-groups/list-groups.component').then(m => m.ListGroupsComponent),
        data: {
          title: 'List Group'
        }
      },
      {
        path: 'navs',
        loadComponent: () => import('./navs/navs.component').then(m => m.NavsComponent),
        data: {
          title: 'Navs & Tabs'
        }
      },
      {
        path: 'pagination',
        loadComponent: () => import('./paginations/paginations.component').then(m => m.PaginationsComponent),
        data: {
          title: 'Pagination'
        }
      },
      {
        path: 'placeholder',
        loadComponent: () => import('./placeholders/placeholders.component').then(m => m.PlaceholdersComponent),
        data: {
          title: 'Placeholder'
        }
      },
      {
        path: 'popovers',
        loadComponent: () => import('./popovers/popovers.component').then(m => m.PopoversComponent),
        data: {
          title: 'Popovers'
        }
      },
      {
        path: 'progress',
        loadComponent: () => import('./progress/progress.component').then(m => m.AppProgressComponent),
        data: {
          title: 'Progress'
        }
      },
      {
        path: 'spinners',
        loadComponent: () => import('./spinners/spinners.component').then(m => m.SpinnersComponent),
        data: {
          title: 'Spinners'
        }
      },
      {
        path: 'tables',
        loadComponent: () => import('./tables/tables.component').then(m => m.TablesComponent),
        data: {
          title: 'Tables'
        }
      },
      {
        path: 'tabs',
        loadComponent: () => import('./tabs/tabs.component').then(m => m.AppTabsComponent),
        data: {
          title: 'Tabs'
        }
      },
      {
        path: 'tooltips',
        loadComponent: () => import('./tooltips/tooltips.component').then(m => m.TooltipsComponent),
        data: {
          title: 'Tooltips'
        }
      },
      {
        path: 'coming-soon',
        loadComponent: () => import('./coming-soon/coming-soon.component').then(m => m.ComingSoonComponent),
        canActivate: [AuthGuard],
        data: {
          title: 'Coming Soon'
        }
      }
    ]
  }
];


