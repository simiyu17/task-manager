import { INavData } from '@coreui/angular';

export const navItems: INavData[] = [
  {
    name: 'Dashboard',
    url: '/base/coming-soon',
    iconComponent: { name: 'cil-speedometer' }
  },
  {
    title: true,
    name: 'Configuration'
  },
  {
    name: 'Email Settings',
    url: '/base/coming-soon',
    iconComponent: { name: 'cil-drop' }
  },
  {
    name: 'Notifications',
    url: '/base/coming-soon',
    linkProps: { fragment: 'headings' },
    iconComponent: { name: 'cil-pencil' }
  },
  {
    name: 'Core',
    title: true
  },
  {
    name: 'Task Management',
    url: '/base/tasks',
    iconComponent: { name: 'cil-drop' }
  },
  {
    title: true,
    name: 'Administration'
  },
  {
    name: 'Users',
    url: '/base/coming-soon',
    iconComponent: { name: 'cil-star' }
  },
  {
    name: 'User Roles',
    url: '/base/coming-soon',
    iconComponent: { name: 'cil-star' }
  },
  {
    name: 'Audit Trails',
    url: '/base/coming-soon',
    iconComponent: { name: 'cil-star' }
  },
  {
    title: true,
    name: 'Extras',
    class: 'mt-auto'
  },
  {
    name: 'Manual Docs',
    url: '/base/coming-soon',
    iconComponent: { name: 'cil-description' }
  }
];
