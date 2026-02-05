import { Component } from '@angular/core';
import {
  CardBodyComponent,
  CardComponent,
  ColComponent,
  RowComponent,
  TextColorDirective
} from '@coreui/angular';

@Component({
  selector: 'app-coming-soon',
  templateUrl: './coming-soon.component.html',
  styleUrls: ['./coming-soon.component.scss'],
  standalone: true,
  imports: [
    RowComponent,
    ColComponent,
    CardComponent,
    CardBodyComponent,
    TextColorDirective
  ]
})
export class ComingSoonComponent {
  constructor() {}
}
