import React, {Component} from 'react';
import {NavLink, Redirect} from 'react-router-dom';
import {RulesTab} from '../components/rules.jsx';


export class Rules extends Component {

  constructor (props) {
    super(props);
    this.state = {
      error: null,
      pending: false,
      ajaxComplete: false,
      filter: null,
      rules: []
    };
  }

  componentDidMount () {
    this.state.filter = this.parseFilterValue(this.props.location.search);
    this.fetchRules(this.state.filter);
  }

  componentWillReceiveProps (nextProps) {
    if (nextProps.location.search !== this.props.location.search) {
      const newFilter = this.parseFilterValue(nextProps.location.search);
      if (newFilter !== this.state.filter) {
        this.setState({filter: newFilter});
        this.fetchRules(newFilter);
      }
    }
  }

  fetchRules (filter) {
    if (filter === null) {
      return;
    }
    this.setState({error: null, pending: true, ajaxComplete: false});
    const {getSupportedRules} = this.props;
    return getSupportedRules(filter)
      .then((response) => {
        this.setState({
          error: null,
          pending: false,
          ajaxComplete: true,
          rules: response.rules
        });
      })
      .catch((error) => {
        console.error(error); // eslint-disable-line no-console

        this.setState({
          pending: false,
          ajaxComplete: true,
          error: error.detail || Rules.DEFAULT_ERROR_MESSAGE,
          rules: [],
        });
        return Promise.reject(error);
      });
  }

  parseFilterValue (search) {
    if (!search) {
      return null;
    }
    const params = new URLSearchParams(search);
    const isActive = params.get('is_active');
    if (isActive !== undefined) {
      return {is_active: isActive === 'true'};
    }
    return null;
  }

  sameFilter (flag) {
    return () => {
      if (this.state.filter === null) {
        return false;
      }
      return this.state.filter.is_active === flag;
    };
  }

  render () {
    if (this.state.filter === null) {
      return <Redirect to={{pathname: '/rules', search: '?is_active=true'}}/>;
    }


    return (
      <div>
        <div className="dc-row">
          <div className="dc-column dc-column--small-12 dc-column--large-7">
            <label className="dc-label filter-navigation__label">Show only</label>
            <div className="dc-btn-group dc-btn-group--in-row">
              <NavLink to={{pathname: '/rules', search: '?is_active=true'}}
                       className="dc-btn dc-btn--small dc-btn--in-btn-group"
                       activeClassName="dc-btn--primary" isActive={this.sameFilter(true)}>Active</NavLink>
              <NavLink to={{pathname: '/rules', search: '?is_active=false'}}
                       className="dc-btn dc-btn--small dc-btn--in-btn-group"
                       activeClassName="dc-btn--primary" isActive={this.sameFilter(false)}>Inactive</NavLink>
            </div>
          </div>
        </div>
        <div className="dc-row">
          <div className="dc-column dc-column--small-12 dc-column--large-7">
            <div className="dc-column__contents">
              <RulesTab error={this.state.error} rules={this.state.rules}/>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

Rules.DEFAULT_ERROR_MESSAGE = 'Ooops not able to load supported rules!';
