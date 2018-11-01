package io.github.dhamith93.projectone;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

class FractionsAdapter extends FragmentPagerAdapter {
    public FractionsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new ProjectsFragment();
            case 1:
                return new MyTasksFragment();
            case 2:
                return new GroupsFragment();
            case 3:
                return new ChatsFragment();
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
                return "PROJECTS";
            case 1:
                return "MY TASKS";
            case 2:
                return "GROUPS";
            case 3:
                return "CHAT";
            default:
                return null;
        }
    }
}
