package com.example.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapter.MusicPageAdapter;
import com.example.entity.Constant;
import com.example.entity.Music;
import com.example.hello.R;
import com.example.service.PlayService;


public class PlayActivity extends FragmentActivity {

	private Button play;
	private Button previous;
	private Button next;
	private TextView musicTitle;// 歌曲名称
	private String songLink; // 歌曲路径
	List<Fragment> fragments;
	private ViewPager pager;

	private int listPosition;
	private List<Music> musicList;// 音乐列表
	private Music music;
	private MusicPageAdapter adapter;
	private Button queueBtn; // 歌曲列表
	private SeekBar music_progressBar; // 歌曲进度
	private TextView currentProgress; // 当前进度消耗的时间
	private TextView finalProgress; // 歌曲时间

	private int currentTime; // 当前歌曲播放时间
	private int duration; // 歌曲长度
	private int flag; // 播放标识

	private boolean isPlaying; // 正在播放
	private boolean isPause; // 暂停

	private MusicImageAlbumFragment musicAlbumFragment = new MusicImageAlbumFragment();

	private PlayerReceiver playerReceiver;
	public static final String UPDATE_ACTION = "com.wwj.action.UPDATE_ACTION"; // 更新动作
	public static final String CTL_ACTION = "com.wwj.action.CTL_ACTION"; // 控制动作
	public static final String MUSIC_CURRENT = "com.wwj.action.MUSIC_CURRENT"; // 音乐当前时间改变动作
	public static final String MUSIC_DURATION = "com.wwj.action.MUSIC_DURATION";// 音乐播放长度改变动作
	public static final String MUSIC_PLAYING = "com.wwj.action.MUSIC_PLAYING"; // 音乐正在播放动作
	public static final String REPEAT_ACTION = "com.wwj.action.REPEAT_ACTION"; // 音乐重复播放动作
	public static final String SHUFFLE_ACTION = "com.wwj.action.SHUFFLE_ACTION";// 音乐随机播放动作
	public static final String SHOW_LRC = "com.wwj.action.SHOW_LRC"; // 通知显示歌词

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_activity_layout);
		findViewById();
		setViewOnclickListener();
		getDataFromBundle();

		fragments = new ArrayList<Fragment>();
		fragments.add(musicAlbumFragment);
		fragments.add(new lrcFragment());
		adapter = new MusicPageAdapter(getSupportFragmentManager(), fragments);
		pager.setAdapter(adapter);
		registerReceiver();
		initView();

	}

	private void initView() {
		musicTitle.setText(music.getSongName());
		musicAlbumFragment.setUrl(music.getSongPicRadio());
		musicAlbumFragment.updateAlbum();
		if(isPlaying){
			play.setBackgroundResource(R.drawable.pause);
		}
	}

	private void findViewById() {
		pager = (ViewPager) findViewById(R.id.pager);
		play = (Button) findViewById(R.id.play);
		previous = (Button) findViewById(R.id.previous);
		next = (Button) findViewById(R.id.next);
		musicTitle = (TextView) findViewById(R.id.musicTitle);
		music_progressBar = (SeekBar) findViewById(R.id.audioTrack);
		currentProgress = (TextView) findViewById(R.id.current_progress);
		finalProgress = (TextView) findViewById(R.id.final_progress);
	}

	private void setViewOnclickListener() {
		ViewOnclickListener viewOnclickListener = new ViewOnclickListener();
		play.setOnClickListener(viewOnclickListener);
		previous.setOnClickListener(viewOnclickListener);
		next.setOnClickListener(viewOnclickListener);
	}

	private void registerReceiver() {
		// 定义和注册广播接收器
		playerReceiver = new PlayerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_ACTION);
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_DURATION);
		registerReceiver(playerReceiver, filter);
	}

	/**
	 * 从Bundle中获取来自HomeActivity中传过来的数据
	 */
	private void getDataFromBundle() {
		Intent intent = getIntent();
		listPosition = intent.getIntExtra("listPosition", 0);
		isPlaying = intent.getBooleanExtra("isPlaying", false);
		isPause = intent.getBooleanExtra("isPause", false);
		musicList = (List<Music>) intent.getSerializableExtra("musicList");
		music = musicList.get(listPosition);
	}

	/**
	 * 控件点击事件
	 * 
	 * @author wwj
	 * 
	 */
	private class ViewOnclickListener implements OnClickListener {
		Intent intent = new Intent();

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.play:
				if (isPlaying) {
					play.setBackgroundResource(R.drawable.pause_selector);
					intent.setClass(PlayActivity.this, PlayService.class);
					intent.putExtra("MSG", Constant.PlayerMsg.PAUSE_MSG);
					startService(intent);
					isPlaying = false;
					isPause = true;
				} else if (isPause) {
					play.setBackgroundResource(R.drawable.play_selector);
					intent.setClass(PlayActivity.this, PlayService.class);
					intent.putExtra("MSG", Constant.PlayerMsg.CONTINUE_MSG);
					startService(intent);
					isPause = false;
					isPlaying = true;
				}
				break;
			case R.id.previous: // 上一首歌曲
				previous_music();
				break;
			case R.id.next: // 下一首歌曲
				next_music();
				break;
			}
		}

		/**
		 * 上一首
		 */
		public void previous_music() {
			play.setBackgroundResource(R.drawable.play_selector);
			listPosition = listPosition - 1;
			if (listPosition >= 0) {
				music = musicList.get(listPosition); // 上一首MP3

				Intent intent = new Intent(PlayActivity.this,PlayService.class);
//				intent.setAction("com.wwj.media.MUSIC_SERVICE");
				intent.putExtra("songLink", music.getSongLink());
				intent.putExtra("listPosition", listPosition);
				intent.putExtra("MSG", Constant.PlayerMsg.PREVIOUS_MSG);
				startService(intent);

			} else {
				listPosition = 0;
				Toast.makeText(PlayActivity.this, "没有上一首了", Toast.LENGTH_SHORT)
						.show();
			}
		}

		/**
		 * 下一首
		 */
		public void next_music() {
			play.setBackgroundResource(R.drawable.play_selector);
			listPosition = listPosition + 1;
			if (listPosition <= musicList.size() - 1) {
				music = musicList.get(listPosition);
				Intent intent = new Intent(PlayActivity.this,PlayService.class);
//				intent.setAction("com.wwj.media.MUSIC_SERVICE");
				intent.putExtra("songLink", music.getSongLink());
				intent.putExtra("listPosition", listPosition);
				intent.putExtra("MSG", Constant.PlayerMsg.NEXT_MSG);
				startService(intent);

			} else {
				listPosition = musicList.size() - 1;
				Toast.makeText(PlayActivity.this, "没有下一首了", Toast.LENGTH_SHORT)
						.show();
			}
		}

	}

	/**
	 * 用来接收从service传回来的广播的内部类
	 * 
	 * @author wwj
	 * 
	 */
	public class PlayerReceiver extends BroadcastReceiver {

		Music music = musicList.get(listPosition);
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MUSIC_CURRENT)) {
				currentTime = intent.getIntExtra("currentTime", -1);
				// currentProgress.setText(MediaUtil.formatTime(currentTime));
				// currentProgress.setText(MediaUtil.formateTime(currentTime));
				int duration = Integer.parseInt(music.getTime()) * 1000;
				long pos = music_progressBar.getMax() * currentTime / duration;
				music_progressBar.setProgress((int) pos);
			} else if (action.equals(MUSIC_DURATION)) {
				int duration = intent.getIntExtra("duration", -1);
				music_progressBar.setMax(duration);
				finalProgress.setText(duration);
			} else if (action.equals(UPDATE_ACTION)) {
				// 获取Intent中的current消息，current代表当前正在播放的歌曲
//				listPosition = intent.getIntExtra("current", -1);
				songLink = musicList.get(listPosition).getSongLink();
				music = musicList.get(listPosition);
				if (listPosition >= 0) {
					musicTitle.setText(music
							.getSongName());
					// musicArtist.setText(musicList.get(listPosition).getArtistName());
				}
				if (listPosition == 0) {
					finalProgress
							.setText(music.getTime());
					play.setBackgroundResource(R.drawable.pause_selector);
					isPause = true;
				}
				musicAlbumFragment.setUrl(music.getSongPicRadio());
				musicAlbumFragment.updateAlbum();
			}
		}
	}
}
