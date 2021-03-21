package com.nevilleantony.prototype.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.utils.URLManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CreateRoomSubpage extends Fragment {

	private final CompositeDisposable compositeDisposable;
	private TextInputLayout textInputLayout;

	public CreateRoomSubpage() {
		compositeDisposable = new CompositeDisposable();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_create_room_subpage, container, false);
		textInputLayout = view.findViewById(R.id.url_text_input_layout);
		TextInputEditText textInputEditText = view.findViewById(R.id.url_edit_text);

		Disposable disposable = RxTextView.textChanges(textInputEditText)
				.skipInitialValue()
				.debounce(500, TimeUnit.MILLISECONDS)
				.subscribe(this::onTextChangeConsumer);

		compositeDisposable.add(disposable);

		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		compositeDisposable.dispose();
	}

	private void onTextChangeConsumer(CharSequence charSequence) {
		Disposable nestedDisposable;

		if (charSequence.toString().isEmpty()) {
			nestedDisposable = Single.fromCallable(() -> {
				textInputLayout.setError(null);

				return true;
			})
					.subscribeOn(AndroidSchedulers.mainThread())
					.subscribe();

			compositeDisposable.add(nestedDisposable);

			return;
		}

		nestedDisposable = Single.fromCallable(() -> URLManager.getURLProperties(charSequence.toString()))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe((urlProperties) -> {
					if (!urlProperties.isReachable) {
						textInputLayout.setError(getString(R.string.invalid_unreachable_url));
					} else {
						textInputLayout.setError(null);
						if (!urlProperties.canAcceptRanges) {
							textInputLayout.setHelperTextTextAppearance(R.style.warningHelperText);
							textInputLayout.setHelperText(getString(R.string.partial_download_unsupported));
						} else {
							textInputLayout.setHelperTextTextAppearance(R.style.successHelperText);
							textInputLayout.setHelperText(getString(R.string.partial_download_supported));
						}
					}
				});

		compositeDisposable.add(nestedDisposable);
	}
}