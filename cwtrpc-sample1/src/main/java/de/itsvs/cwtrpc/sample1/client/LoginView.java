/*
 *  Copyright 2011 IT Services VS GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.itsvs.cwtrpc.sample1.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import de.itsvs.cwtrpc.sample1.client.service.LoginService;
import de.itsvs.cwtrpc.sample1.client.service.LoginServiceAsync;
import de.itsvs.cwtrpc.security.BadCredentialsException;
import de.itsvs.cwtrpc.security.DisabledException;
import de.itsvs.cwtrpc.security.LockedException;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class LoginView extends Composite {
	private final static LoginWidgetUiBinder uiBinder = GWT
			.create(LoginWidgetUiBinder.class);

	interface LoginWidgetUiBinder extends UiBinder<Widget, LoginView> {
		/* nothing to define */
	}

	private final LoginServiceAsync loginService = GWT
			.create(LoginService.class);

	@UiField
	SimplePanel errorMessagePanel;

	@UiField
	Label errorMessageLabel;

	@UiField
	Button loginButton;

	@UiField
	TextBox userNameField;

	@UiField
	PasswordTextBox passwordField;

	public LoginView() {
		initWidget(uiBinder.createAndBindUi(this));

		errorMessagePanel.setVisible(false);
		loginButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				loginService.login(userNameField.getValue(),
						passwordField.getValue(), new AsyncCallback<String>() {
							public void onSuccess(String roleNames) {
								final RootPanel rp = RootPanel.get();

								rp.clear();
								rp.add(new SampleView(roleNames));
							}

							public void onFailure(Throwable caught) {
								if (caught instanceof BadCredentialsException) {
									Window.alert("Invalid user name or password!");
								} else if (caught instanceof DisabledException) {
									Window.alert("Account has been disabled!");
								} else if (caught instanceof LockedException) {
									Window.alert("Account has been locked!");
								} else {
									Window.alert(caught.toString());
								}
							}
						});
			}
		});
	}

	public void setErrorMessage(String errorMessage) {
		errorMessageLabel.setText(errorMessage);
		errorMessagePanel.setVisible(true);
	}
}
