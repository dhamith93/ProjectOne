package io.github.dhamith93.projectone.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import io.github.dhamith93.projectone.fragments.*;

public class FragmentsAdapter extends FragmentPagerAdapter {
    public FragmentsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new NotificationFragment();
            case 1:
                return new ProjectsFragment();
            case 2:
                return new MyTasksFragment();
            case 3:
                return new GroupsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() { return 4; }

    @Override
    public CharSequence getPageTitle(int i) {
        switch (i) {
            case 0:
                return "NOTIFICATIONS";
            case 1:
                return "PROJECTS";
            case 2:
                return "MY TASKS";
            case 3:
                return "GROUPS";
            default:
                return null;
        }
    }
}
