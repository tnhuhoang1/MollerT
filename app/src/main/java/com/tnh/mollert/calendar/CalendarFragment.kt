package com.tnh.mollert.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.tnh.mollert.R
import com.tnh.mollert.databinding.CalendarFragmentBinding
import com.tnh.mollert.utils.getDate
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.logAny
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalendarFragment: DataBindingFragment<CalendarFragmentBinding>(R.layout.calendar_fragment) {
    private var container: ViewGroup? = null
    private val viewModel by viewModels<CalendarViewModel>()
    private val adapter by lazy {
        DayAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.container = container
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun doOnCreateView() {
        setupToolbar()

        binding.calendarFragmentRecycler.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
    }

    private fun setupObserver() {
        safeObserve(viewModel.cardHasDate){ listCard->
            val listDayWithDeadline = mutableListOf<DayWithDeadline>()
            if(listCard.size == 1){
                val dayWithDeadline = DayWithDeadline(
                    listCard.first().startDate.getDate("E"),
                    listCard.first().startDate.getDate("dd"),
                    listCard
                )
                listDayWithDeadline.add(dayWithDeadline)
            }else{
                var i = 0
                while (i < listCard.size){
                    val dayWithDeadline = DayWithDeadline("", "")
                    dayWithDeadline.day = listCard[i].startDate.getDate("E")
                    dayWithDeadline.dayNumber = listCard[i].startDate.getDate("dd")
                    val cards = mutableListOf(listCard[i])
                    for(j in i + 1 until listCard.size){
                        if(listCard[i].startDate.getDate() == listCard[j].startDate.getDate()){
                            cards.add(listCard[j])
                        }else{
                            i = j - 1
                            break
                        }
                    }
                    dayWithDeadline.listCard = cards
                    listDayWithDeadline.add(dayWithDeadline)
                    i++
                }
            }
            adapter.submitList(listDayWithDeadline)
        }
    }

    private fun setupToolbar(){
        binding.calendarFragmentToolbar.apply {
            twoActionToolbarTitle.text = "Calendar"
        }
    }

}