import React from 'react';
import {Msg} from './dress-code.jsx';

export class CompleteMsg extends React.Component{
  constructor(props) { super(props) }

  render(){
    if( this.props.error ){

      return <Msg type="error" title="ERROR" text={this.props.error} onCloseButtonClick={this.props.onCloseError} />;

    }else if( this.props.ajaxComplete && !this.props.violations.length ){

      return <Msg type="success" title="Good Job !" text="The swagger file does not have any violations" onCloseButtonClick={this.props.onCloseSuccess} />

    }else{

      return null;

    }
  }
}
