package dev.dmanluc.openbankmobiletest.data.repository

import arrow.core.Either
import dev.dmanluc.openbankmobiletest.data.local.datasource.CharactersLocalDataSource
import dev.dmanluc.openbankmobiletest.data.remote.datasource.CharactersRemoteDataSource
import dev.dmanluc.openbankmobiletest.domain.model.ApiError
import dev.dmanluc.openbankmobiletest.domain.model.Character
import dev.dmanluc.openbankmobiletest.domain.repository.CharactersRepository
import dev.dmanluc.openbankmobiletest.utils.orFalse
import kotlinx.coroutines.flow.Flow

class CharactersRepositoryImpl(
    private val charactersLocalDataSource: CharactersLocalDataSource,
    private val charactersRemoteDataSource: CharactersRemoteDataSource,
    private val dataSourceManager: DataSourceManager
) : CharactersRepository {

    override suspend fun getCharacters(
        pagingOffset: Int,
        forceRefresh: Boolean
    ): Flow<Either<ApiError, List<Character>>> {
        return dataSourceManager.performDataRequest(
            localDataQuery = { loadCharactersFromLocal(pagingOffset) },
            fetchFromRemoteSource = { fetchCharactersFromRemote(pagingOffset) },
            saveFetchResult = { remoteFetchedCharacters, isLocalDataInvalid ->
                saveRemoteDataToLocal(
                    remoteFetchedCharacters,
                    isLocalDataInvalid
                )
            },
            shouldInvalidateLocalData = { forceRefresh },
            shouldFetch = { savedCharacters ->
                checkIfRemoteSourceFetching(
                    savedCharacters,
                    forceRefresh
                )
            }
        )
    }

    private suspend fun loadCharactersFromLocal(pagingOffset: Int): List<Character> {
        return charactersLocalDataSource.getCharacters(pagingOffset)
    }

    private suspend fun fetchCharactersFromRemote(pagingOffset: Int): List<Character> {
        return charactersRemoteDataSource.getCharacters(pagingOffset)
    }

    private suspend fun saveRemoteDataToLocal(
        characters: List<Character>,
        invalidateLocalData: Boolean
    ) {
        if (invalidateLocalData) {
            charactersLocalDataSource.replaceAllCharacters(characters)
        } else {
            charactersLocalDataSource.saveCharacters(characters)
        }
    }

    private fun checkIfRemoteSourceFetching(
        savedCharactersFromLocalDataSource: List<Character>?,
        forceRefresh: Boolean
    ): Boolean {
        return forceRefresh || savedCharactersFromLocalDataSource?.isEmpty().orFalse()
    }

}