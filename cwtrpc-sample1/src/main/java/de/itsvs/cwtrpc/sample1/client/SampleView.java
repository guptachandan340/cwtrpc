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
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import de.itsvs.cwtrpc.sample1.client.service.LoginService;
import de.itsvs.cwtrpc.sample1.client.service.LoginServiceAsync;
import de.itsvs.cwtrpc.sample1.client.service.SampleService;
import de.itsvs.cwtrpc.sample1.client.service.SampleServiceAsync;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class SampleView extends Composite {
	private final static MainWidgetUiBinder uiBinder = GWT
			.create(MainWidgetUiBinder.class);

	interface MainWidgetUiBinder extends UiBinder<Widget, SampleView> {
		/* nothing to define */
	}

	private final LoginServiceAsync loginService = GWT
			.create(LoginService.class);

	private final SampleServiceAsync sampleService = GWT
			.create(SampleService.class);

	@UiField
	Label roleNamesLabel;

	@UiField
	Button infoButton;

	@UiField
	Button demoInfoButton;

	@UiField
	Button logoutButton;

	public SampleView(String roleNames) {
		initWidget(uiBinder.createAndBindUi(this));

		roleNamesLabel.setText(roleNames);

		infoButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				sampleService.getInfo(new AsyncCallback<String>() {
					public void onSuccess(String result) {
						Window.alert(result);
					}

					public void onFailure(Throwable caught) {
						if (!checkInvalidSession(caught)) {
							Window.alert(caught.toString());
						}
					}
				});
			}
		});
		demoInfoButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				sampleService.getDemoInfo(new AsyncCallback<String>() {
					public void onSuccess(String result) {
						Window.alert(result);
					}

					public void onFailure(Throwable caught) {
						if (!checkInvalidSession(caught)
								&& !checkForbidden(caught)) {
							Window.alert(caught.toString());
						}
					}
				});
			}
		});
		logoutButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				loginService.logout(new AsyncCallback<Void>() {
					public void onSuccess(Void result) {
						final RootPanel rp = RootPanel.get();

						rp.clear();
						rp.add(new LoginView());
					}

					public void onFailure(Throwable caught) {
						Window.alert(caught.toString());
					}
				});
			}
		});
	}

	protected boolean checkInvalidSession(Throwable caught) {
		if (caught instanceof StatusCodeException) {
			final StatusCodeException sce;

			sce = (StatusCodeException) caught;
			if ((sce.getStatusCode() == 400)
					&& "INVALID_SESSION".equals(sce.getEncodedResponse())) {
				handleInvalidSession();
				return true;
			}
		}
		return false;
	}

	protected boolean checkForbidden(Throwable caught) {
		if (caught instanceof StatusCodeException) {
			final StatusCodeException sce;

			sce = (StatusCodeException) caught;
			if (sce.getStatusCode() == 403) {
				Window.alert("You do not have role ROLE_DEMO."
						+ "\nAccess is forbidden!");
				return true;
			}
		}
		return false;
	}

	protected void handleInvalidSession() {
		final RootPanel rp = RootPanel.get();
		final LoginView view;

		view = new LoginView();
		view.setErrorMessage("Your session has expired!");

		rp.clear();
		rp.add(view);
	}
}
