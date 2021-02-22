package dev.dmanluc.openbankmobiletest.presentation.characters

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.dmanluc.openbankmobiletest.R
import dev.dmanluc.openbankmobiletest.databinding.FragmentCharacterListBinding
import dev.dmanluc.openbankmobiletest.presentation.base.BaseFragment
import dev.dmanluc.openbankmobiletest.presentation.base.BaseViewModel
import dev.dmanluc.openbankmobiletest.utils.EndlessRecyclerViewScrollListener
import dev.dmanluc.openbankmobiletest.utils.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * @author   Daniel Manrique Lucas <dmanluc91@gmail.com>
 * @version  1
 *
 * First flow fragment which shows characters list from remote data source and user can navigate to its detail when clicking on it
 *
 */
class CharactersFragment : BaseFragment(R.layout.fragment_character_list) {

    private val viewModel: CharactersFragmentViewModel by viewModel()

    private val binding by viewBinding(FragmentCharacterListBinding::bind)

    private val charactersAdapter by lazy {
        CharactersPagingAdapter({})
    }

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureRecyclerView()
        setupUi()
    }

    private fun configureRecyclerView() {
        binding.characterRecyclerList.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = charactersAdapter
            addOnScrollListener(EndlessRecyclerViewScrollListener { _: Int, totalItemsCount: Int, _: RecyclerView ->
                viewModel.pagingLoadTracker.appendFrom(totalItemsCount)
                charactersAdapter.setPagingLoading()
                viewModel.loadCharacters()
            })
        }
    }

    private fun setupUi() {
        viewModel.characterListLiveData.observe(viewLifecycleOwner) { charactersList ->
            charactersAdapter.setAdapterItems(charactersList, viewModel.pagingLoadTracker)
        }
    }

}